package com.r8.autozmi;

import com.google.inject.Inject;
import net.runelite.client.plugins.iutils.scripts.UtilsScript;

public abstract class Task extends UtilsScript {
  @Inject
  protected ZmiPlugin plugin;

  @Inject
  protected ZmiConfig config;

  public Task() {
  }

  /**
   * Check if the conditions for finishing the task are met.
   */
  public abstract boolean validate();

  /**
   * Reset any values that were stored in the task class.
   */
  public abstract void reset();

  // TODO: Maybe make a human readable state?

  /**
   * Get what task is being done in a string form.
   */
  public abstract String state();

  /**
   * Will be run on every game tick, so long as it is the current task in the set.
   */
  public abstract void run();
}
