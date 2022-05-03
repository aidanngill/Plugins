package com.r8.autoconstruction;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.r8.autoconstruction.tasks.AwaitButler;
import com.r8.autoconstruction.tasks.BuildSeats;
import com.r8.autoconstruction.tasks.CallButler;
import com.r8.autoconstruction.tasks.RemoveSeats;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
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
    name = "Auto Construction",
    description = "Automate the construction of teak benches",
    enabledByDefault = false,
    tags = {"ramadan8", "construction"}
)
@PluginDependency(iUtils.class)
@Slf4j
public class ConstructionPlugin extends UtilsScript {
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
   * Amount of Smithing experience the user had upon starting the bot. Used for the overlay.
   */
  @Getter(AccessLevel.PACKAGE)
  private int initialXpAmount;

  /**
   * Amount of Gold (GP) earned since the bot was started. Used for the overlay.
   */
  @Getter(AccessLevel.PACKAGE)
  private long goldAmount;

  /**
   * List of tasks to run. Will be run sequentially and then repeated while {@code isProcessRunning == true}.
   * This <b>must</b> be initialized in a client thread, as the injector will only work there.
   */
  private final TaskSet taskSet = new TaskSet();

  @Provides
  ConstructionConfig getConfig(ConfigManager configManager) {
    return configManager.getConfig(ConstructionConfig.class);
  }

  @Override
  protected void startUp() {
    if (taskSet.getSize() == 0) {
      taskSet.addAll(
          injector.getInstance(BuildSeats.class),
          injector.getInstance(RemoveSeats.class),
          injector.getInstance(CallButler.class),
          injector.getInstance(BuildSeats.class),
          injector.getInstance(AwaitButler.class),
          injector.getInstance(RemoveSeats.class)
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
    initialXpAmount = client.getSkillExperience(Skill.CONSTRUCTION);
    goldAmount = 0;
  }

  @Subscribe
  private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
    if (!configButtonClicked.getGroup().equalsIgnoreCase("sconstruction")) {
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
  public void onChatMessage(ChatMessage chatMessage) {
    if (chatMessage.getMessage().contains("Your servant takes some payment from the moneybag")) {
      addGoldAmount(-10_000);
    }
  }

  @Subscribe
  public void onGameTick(GameTick gameTick) {
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
}
