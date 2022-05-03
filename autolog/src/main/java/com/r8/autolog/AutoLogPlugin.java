package com.r8.autolog;

import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

enum AutoLogState {
  Empty,
  Logout,
  LogoutTab,
  LogoutButton
}

@Extension
@PluginDescriptor(
    name = "Auto Log",
    description = "Escape sticky situations with ease",
    enabledByDefault = false,
    tags = {"ramadan8", "logout", "wilderness"}
)
@Slf4j
public class AutoLogPlugin extends Plugin {
  @Inject
  private Client client;

  @Inject
  private Utility utility;

  @Inject
  private AutoLogConfig config;

  private AutoLogState state = AutoLogState.Empty;

  @Provides
  AutoLogConfig getConfig(ConfigManager configManager) {
    return configManager.getConfig(AutoLogConfig.class);
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged gameStateChanged) {
    if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
      state = AutoLogState.Empty;
    }
  }

  @Subscribe
  public void onPlayerSpawned(PlayerSpawned playerSpawned) {
    onPlayerSeen(playerSpawned.getPlayer());
  }

  @Subscribe
  public void onGameTick(GameTick gameTick) {
    Player thisPlayer = client.getLocalPlayer();

    if (thisPlayer == null || client.getGameState() != GameState.LOGGED_IN) {
      return;
    }

    switch (state) {
      case Logout: {
        utility.sendKey(KeyEvent.VK_ESCAPE);
        state = AutoLogState.LogoutTab;
      }
      break;
      case LogoutTab: {
        if (client.getVar(VarClientInt.INVENTORY_TAB) == 10) {
          Widget logoutBtn = client.getWidget(182, 8);
          Widget logoutDoorBtn = client.getWidget(69, 23);

          if (logoutBtn != null || logoutDoorBtn != null) {
            state = AutoLogState.LogoutButton;
            utility.mouseClick();
          }
        } else {
          client.runScript(915, 10);
        }
      }
      break;
    }

    if (state != AutoLogState.Empty) {
      return;
    }

    for (Player targetPlayer : client.getPlayers()) {
      onPlayerSeen(targetPlayer);
    }
  }

  @Subscribe
  public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
    if (state == AutoLogState.LogoutButton) {
      Widget logoutBtn = client.getWidget(182, 8);
      Widget logoutDoorBtn = client.getWidget(69, 23);

      int param1 = -1;

      if (logoutBtn != null) {
        param1 = logoutBtn.getId();
      } else if (logoutDoorBtn != null) {
        param1 = logoutDoorBtn.getId();
      }

      if (param1 == -1) {
        menuOptionClicked.consume();
        return;
      }

      utility.menuAction(
          menuOptionClicked,
          "Logout",
          "",
          1,
          MenuAction.CC_OP,
          -1,
          param1
      );

      state = AutoLogState.Empty;
    }
  }

  /**
   * Determines whether the player is currently in the Wilderness using the Wilderness level widget.
   *
   * @return Whether the player is in the Wilderness.
   */
  private Boolean isInWilderness() {
    Widget wildernessWidget = client.getWidget(90, 44);

    return wildernessWidget != null && !wildernessWidget.isHidden();
  }

  /**
   * Get the lowest and highest possible combat level that can attack you in the Wilderness.
   *
   * @return Optional pair of integers being the minimum and maximum combat level.
   */
  private Optional<Pair<Integer, Integer>> getWildernessLevelRange() {
    Widget wildernessWidget = client.getWidget(90, 50);

    if (wildernessWidget == null) {
      return Optional.empty();
    }

    Pattern r = Pattern.compile("<br>(\\d+)-(\\d+)");
    Matcher m = r.matcher(wildernessWidget.getText());

    return m.find()
        ? Optional.of(new Pair<>(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))))
        : Optional.empty();
  }

  /**
   * Determine whether we should log out for the given player.
   *
   * @param targetPlayer New player spotted by the client.
   */
  private void onPlayerSeen(Player targetPlayer) {
    if (state != AutoLogState.Empty) {
      return;
    }

    if (config.wildernessOnly() && !isInWilderness()) {
      return;
    }

    List<Boolean> conditions = new ArrayList<>();

    if (config.wildernessLevelOnly()) {
      Optional<Pair<Integer, Integer>> maybeWildernessRange = getWildernessLevelRange();

      if (maybeWildernessRange.isPresent()) {
        Pair<Integer, Integer> wildernessRange = maybeWildernessRange.get();
        Integer targetCombatLevel = targetPlayer.getCombatLevel();

        conditions.add(wildernessRange.getFirst() <= targetCombatLevel &&
            targetCombatLevel <= wildernessRange.getSecond());
      }
    }

    if (config.skulledOnly()) {
      conditions.add(targetPlayer.getSkullIcon() != null);
    }

    Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

    if (inventoryWidget != null) {
      conditions.add(inventoryWidget.getWidgetItems().size() >= config.inventoryThreshold());
    }

    if (conditions.size() > 0 && !conditions.contains(false)) {
      state = AutoLogState.Logout;
      log.info("All conditions were met for user '{}', initiated a logout.",
          targetPlayer.getName());
    }
  }
}
