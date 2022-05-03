package com.r8.autozmi;


import com.google.inject.Inject;
import com.google.inject.Provides;
import com.r8.autozmi.tasks.ClimbLadder;
import com.r8.autozmi.tasks.CraftRunes;
import com.r8.autozmi.tasks.EnableRun;
import com.r8.autozmi.tasks.RestoreHealth;
import com.r8.autozmi.tasks.RestorePrayer;
import com.r8.autozmi.tasks.RestoreStamina;
import com.r8.autozmi.tasks.TeleportAway;
import com.r8.autozmi.tasks.WithdrawRunes;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
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

@Extension
@PluginDescriptor(
    name = "Auto ZMI",
    description = "Automates ZMI runecrafting",
    enabledByDefault = false,
    tags = {"ramadan8", "zmi", "runecraft"}
)
@PluginDependency(iUtils.class)
@Slf4j
public class ZmiPlugin extends UtilsScript {
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

  /**
   * Whether the bot process is running.
   */
  @Getter(AccessLevel.PACKAGE)
  private boolean isProcessRunning = false;

  /**
   * Whether the current task we're doing is running. Avoids re-running tasks if they're not finished already.
   */
  private boolean isTaskRunning = false;

  /**
   * Exact time the bot was started. Used to determine hourly rates for experience and gold.
   */
  @Getter(AccessLevel.PACKAGE)
  private Instant startTime;

  /**
   * Amount of RUNECRAFT experience the user had upon starting the bot. Used for the overlay.
   */
  @Getter(AccessLevel.PACKAGE)
  private int initialXpAmount;

  /**
   * Amount of Gold (GP) earned since the bot was started. Used for the overlay.
   */
  @Getter(AccessLevel.PACKAGE)
  private long goldAmount;

  private final TaskSet taskSet = new TaskSet();

  @Provides
  ZmiConfig getConfig(ConfigManager configManager) {
    return configManager.getConfig(ZmiConfig.class);
  }

  @Override
  protected void startUp() {
    if (taskSet.getSize() == 0) {
      taskSet.addAll(
          injector.getInstance(EnableRun.class),
          injector.getInstance(RestoreStamina.class),
          injector.getInstance(RestoreHealth.class),
          injector.getInstance(WithdrawRunes.class),
          injector.getInstance(CraftRunes.class),
          injector.getInstance(TeleportAway.class),
          injector.getInstance(RestorePrayer.class),
          injector.getInstance(ClimbLadder.class)
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

  /**
   * Reset any variables that may have been changed during previous runs of the bot.
   */
  private void reset() {
    startTime = Instant.now();
    initialXpAmount = client.getSkillExperience(Skill.RUNECRAFT);
    goldAmount = 0;
  }

  @Subscribe
  private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
    if (!configButtonClicked.getGroup().equalsIgnoreCase("szmi")) {
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
  public void onGameTick(GameTick gameTick) {
    // TODO: List actions from the bank inventory to see if we need to refill.
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
      executorService.submit(() -> chatbox.continueChats());
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

  public Instant getPlannedBreak() {
    return reflectBreakHandler.getPlannedBreak(this);
  }
}
