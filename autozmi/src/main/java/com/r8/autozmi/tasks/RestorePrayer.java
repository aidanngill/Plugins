package com.r8.autozmi.tasks;

import com.r8.autozmi.Task;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.ui.Prayer;

public class RestorePrayer extends Task {
  private boolean hasStartedRun = false;

  @Inject
  Client client;

  @Override
  public boolean validate() {
    return client.getBoostedSkillLevel(Skill.PRAYER) >= client.getRealSkillLevel(Skill.PRAYER);
  }

  @Override
  public void reset() {
    hasStartedRun = false;
  }

  @Override
  public String state() {
    return "RESTORE_PRAYER";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    // Turn off our prayers.
    for (var prayer : Prayer.values()) {
      if (prayers.active(prayer)) {
        prayers.setEnabled(prayer, false);
      }
    }

    // Teleport using the Ourania Teleport.
    // TODO walking.walkTo(new Position(2455, 3232, 0));
    game.objects().withName("Chaos altar").first().interact("Pray-at");
  }
}
