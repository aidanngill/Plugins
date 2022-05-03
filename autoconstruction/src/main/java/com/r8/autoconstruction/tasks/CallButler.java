package com.r8.autoconstruction.tasks;

import com.r8.autoconstruction.Task;
import net.runelite.api.MenuAction;

public class CallButler extends Task {
  private boolean hasFinishedRun = false;

  @Override
  public boolean validate() {
    return hasFinishedRun;
  }

  @Override
  public void reset() {
    hasStartedRun = false;
    hasFinishedRun = false;
  }

  @Override
  public String state() {
    return "CALL_BUTLER";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    var butlerNpc = game.npcs().withName("Demon butler").first();

    if (butlerNpc == null || butlerNpc.position().distanceTo(game.localPlayer().position()) > 1) {
      // Open settings menu.
      game.interactionManager().interact(1, MenuAction.CC_OP.getId(), -1, 10551342);
      game.sleepDelay();

      // Open house options.
      game.interactionManager().interact(1, MenuAction.CC_OP.getId(), -1, 7602250);
      game.tick();

      // Call the servant.
      game.interactionManager().interact(1, MenuAction.CC_OP.getId(), -1, 24248339);
      game.sleepDelay();
    } else {
      butlerNpc.interact("Talk-to");
    }

    doQuickChat(1);

    hasFinishedRun = true;
  }
}
