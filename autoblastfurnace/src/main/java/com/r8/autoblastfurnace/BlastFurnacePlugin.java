package com.r8.autoblastfurnace;

import com.google.inject.Provides;
import com.r8.autoblastfurnace.tasks.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.scripts.UtilsScript;
import net.runelite.client.plugins.iutils.ui.Chatbox;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.ExecutorService;

/** Plugin for automating the Blast Furnace minigame. */
@Extension
@PluginDescriptor(
    name = "Auto Blast Furnace",
    description = "Automates the Blast Furnace minigame",
    enabledByDefault = false,
    tags = {"ramadan8", "blast", "furnace", "smithing"}
)
@PluginDependency(iUtils.class)
@Slf4j
public class BlastFurnacePlugin extends UtilsScript {
  @Inject
  Client client;

  @Inject
  ExecutorService executorService;

  @Inject
  OverlayManager overlayManager;

  @Inject
  TaskOverlay taskOverlay;

  @Inject
  ReflectBreakHandler reflectBreakHandler;

  /** Whether the bot process is running. */
  @Getter(AccessLevel.PACKAGE)
  private boolean isProcessRunning = false;

  /** Whether the current task we're doing is running. Avoids re-running
   * tasks if they're not finished already. */
  private boolean isTaskRunning = false;

  /** Exact time the bot was started. Used to determine hourly rates for experience and gold. */
  @Getter(AccessLevel.PACKAGE)
  private Instant startTime;

  /** Amount of Smithing experience the user had upon starting the bot. Used for the overlay. */
  @Getter(AccessLevel.PACKAGE)
  private int initialXpAmount;

  /** Amount of Gold (GP) earned since the bot was started. Used for the overlay. */
  @Getter(AccessLevel.PACKAGE)
  private long goldAmount;

  /** List of tasks to run. Will be run sequentially and then repeated while
   * {@code isProcessRunning == true}. This <b>must</b> be initialized in a
   * client thread, as the injector will only work there. */
  private final TaskSet taskSet = new TaskSet();

  @Provides
  @SuppressWarnings("unused")
  BlastFurnaceConfig getConfig(ConfigManager configManager) {
    return configManager.getConfig(BlastFurnaceConfig.class);
  }

  @Override
  protected void startUp() {
    if (taskSet.getSize() == 0) {
      taskSet.addAll(
          injector.getInstance(EnableRun.class),
          injector.getInstance(RefillCoffer.class),
          injector.getInstance(DrinkStaminaPotion.class),
          injector.getInstance(FetchOres.class),
          injector.getInstance(DepositOres.class),
          injector.getInstance(AwaitBarPickup.class),
          injector.getInstance(BarPickup.class),
          injector.getInstance(DepositBars.class),
          injector.getInstance(WithdrawCoffer.class)
      );
    }

    reset();
    isProcessRunning = false;

    reflectBreakHandler.registerPlugin(this);
  }

  @Override
  protected void shutDown() {
    overlayManager.remove(taskOverlay);

    reset();
    isProcessRunning = false;

    reflectBreakHandler.stopPlugin(this);
    reflectBreakHandler.unregisterPlugin(this);
  }

  /** Reset any variables that may have been changed during previous runs of the bot. */
  private void reset() {
    startTime = Instant.now();
    initialXpAmount = client.getSkillExperience(Skill.SMITHING);
    goldAmount = 0;
  }

  @Subscribe
  @SuppressWarnings("unused")
  private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
    if (!configButtonClicked.getGroup().equalsIgnoreCase("sblastfurnace")) {
      return;
    }

    if (configButtonClicked.getKey().equals("startButton")) {
      isProcessRunning = !isProcessRunning;

      if (isProcessRunning) {
        reset();

        reflectBreakHandler.startPlugin(this);
        overlayManager.add(taskOverlay);
      } else {
        reflectBreakHandler.stopPlugin(this);
        overlayManager.remove(taskOverlay);
      }
    }
  }

  @Subscribe
  @SuppressWarnings("unused")
  private void onGameTick(GameTick gameTick) {
    if (!isProcessRunning) {
      return;
    }

    if (reflectBreakHandler.isBreakActive(this)) {
      return;
    }

    // Get the task first to bump the wrap before we do anything with it.
    Task task = taskSet.getValidTask();

    if (reflectBreakHandler.shouldBreak(this)) {
      if (taskSet.getIsWrapped()) {
        reflectBreakHandler.startBreak(this);
        taskSet.resetTaskIndex();

        return;
      }

      taskSet.resetWrap();
    }

    if (task == null) {
      return;
    }

    // Check if we've levelled up.
    if (chatbox.chatState().equals(Chatbox.ChatState.LEVEL_UP)) {
      chatbox.continueChats();
    }

    if (isTaskRunning) {
      return;
    }

    executorService.submit(() -> {
      isTaskRunning = true;
      task.run();
      isTaskRunning = false;
    });
  }

  public void addGoldAmount(int goldAmount) {
    this.goldAmount += goldAmount;
  }

  /** Get the amount of gold in the coffer. */
  public Integer getCofferAmount() {
    return game.getFromClientThread(() -> client.getVarbitValue(Varbits.BLAST_FURNACE_COFFER));
  }

  /** Get the human-readable form of the currently running task. */
  public String getStateText() {
    Task currentTask = taskSet.getCurrentTask();

    if (currentTask == null) {
      return "IDLE";
    }

    if (reflectBreakHandler.isBreakActive(this)) {
      return "BREAK";
    }

    return currentTask.state();
  }

  /** Get the next instance of a break. */
  public Instant getPlannedBreak() {
    return reflectBreakHandler.getPlannedBreak(this);
  }
}
