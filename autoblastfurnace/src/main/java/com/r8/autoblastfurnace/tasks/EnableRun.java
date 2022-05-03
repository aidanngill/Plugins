package com.r8.autoblastfurnace.tasks;

import com.r8.autoblastfurnace.Task;
import net.runelite.api.Client;

import javax.inject.Inject;

/** Enable sprint if it is not already turned on. Only done once at
 * the beginning of the task list to avoid spamming, although it
 * will be checked every time. */
public class EnableRun extends Task {
  @Inject
  protected Client client;

  private boolean hasStartedRun = false;

  @Override
  public boolean validate() {
    return walking.isRunning();
  }

  @Override
  public void reset() {
    hasStartedRun = false;
  }

  @Override
  public String state() {
    return "ENABLE_RUN";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;
    walking.setRun(true);
  }
}
