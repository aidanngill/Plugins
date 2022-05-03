package com.r8.autozmi.tasks;

import com.r8.autozmi.Task;

public class ClimbLadder extends Task {
  private boolean hasStartedRun = false;

  @Override
  public boolean validate() {
    return game.localPlayer().position().regionID() == 12119;
  }

  @Override
  public void reset() {
    hasStartedRun = false;
  }

  @Override
  public String state() {
    return "CLIMB_LADDER";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    game
        .objects()
        .withName("Ladder")
        .nearest(game.objects().withName("Chaos altar").first().position())
        .interact("Climb");
  }
}
