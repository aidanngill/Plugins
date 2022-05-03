package com.r8.autozmi.tasks;

import com.r8.autozmi.Task;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.iutils.api.Spells;

public class TeleportAway extends Task {
  private boolean hasStartedRun = false;
  private WorldPoint previousLocation = null;

  @Override
  public boolean validate() {
    return game.localPlayer().position().regionID() == 9778;
  }

  @Override
  public void reset() {
    hasStartedRun = false;
    previousLocation = null;
  }

  @Override
  public String state() {
    return "TELEPORT_AWAY";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;
    previousLocation = game.localPlayer().position().toWorldPoint();

    // Teleport using the Ourania Teleport.
    game.widget(Spells.OURANIA_TELEPORT.getInfo()).interact("Cast");
  }
}
