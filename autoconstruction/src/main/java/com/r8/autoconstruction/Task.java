package com.r8.autoconstruction;

import javax.inject.Inject;
import net.runelite.client.plugins.iutils.scripts.UtilsScript;
import net.runelite.client.plugins.iutils.ui.Chatbox;

public abstract class Task extends UtilsScript {
  @Inject
  protected ConstructionPlugin plugin;

  @Inject
  protected ConstructionConfig config;

  protected boolean hasStartedRun = false;

  public Task() {
  }

  /**
   * Check if the conditions for finishing the task are met.
   */
  public abstract boolean validate();

  /**
   * Reset any values that were stored in the task class.
   */
  public void reset() {
    hasStartedRun = false;
  }

  // TODO: Maybe make a human readable state?

  /**
   * Get what task is being done in a string form.
   */
  public String state() {
    return "UNKNOWN";
  }

  /**
   * Will be run on every game tick, so long as it is the current task in the set.
   */
  public void run() {
  }

  protected void doQuickChat(int index) {
    game.waitUntil(() -> chatbox.chatState().equals(Chatbox.ChatState.OPTIONS_CHAT));

    if (game.widget(219, 1, index).text() != null) {
      game.widget(219, 1, index).select();
    }
  }
}
