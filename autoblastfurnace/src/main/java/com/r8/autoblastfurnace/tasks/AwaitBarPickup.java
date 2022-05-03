package com.r8.autoblastfurnace.tasks;

import com.r8.autoblastfurnace.OreType;
import com.r8.autoblastfurnace.Task;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.plugins.iutils.scene.Position;

import javax.inject.Inject;

/** Between dropping the ore off at the conveyor belt and picking up the ore from the dispenser we
 * must do the following tasks.
 *
 * <ul>
 *     <li>Make sure that we are wearing Goldsmith gauntlets if we are smelting gold, for the increased experience.</li>
 *     <li>Walk to the right side of the dispenser, as it is on the way to the bank.</li>
 * </ul>
 */
public class AwaitBarPickup extends Task {
  @Inject
  protected Client client;

  private boolean hasStartedRun = false;

  // Our original Smithing XP amount, before any XP drops.
  private Integer previousExperience = null;

  @Override
  public boolean validate() {
    return previousExperience != null && client.getSkillExperience(Skill.SMITHING) != previousExperience;
  }

  @Override
  public void reset() {
    hasStartedRun = false;
    previousExperience = null;
  }

  @Override
  public String state() {
    return "AWAIT_BAR_PICKUP";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;
    previousExperience = client.getSkillExperience(Skill.SMITHING);

    // Walk to the right of the pickup zone, closer to the bank.
    walking.tile(new Position(1940, 4962, 0)).walkTo();

    // Wear Goldsmith gauntlets if we're smelting gold to increase XP gain.
    if (config.oreType().equals(OreType.GOLD_ORE)) {
      if (!game.equipment().withName("Goldsmith gauntlets").exists()) {
        game.inventory().withName("Goldsmith gauntlets").first().interact("Wear");
        game.sleepDelay();
      }
    }
  }
}
