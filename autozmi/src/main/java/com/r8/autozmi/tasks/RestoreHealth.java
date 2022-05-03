package com.r8.autozmi.tasks;

import com.google.inject.Inject;
import com.r8.autozmi.Task;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;

public class RestoreHealth extends Task {
  private boolean hasStartedRun = false;

  @Inject
  Client client;

  @Override
  public boolean validate() {
    return client.getBoostedSkillLevel(Skill.HITPOINTS) >=
        (client.getRealSkillLevel(Skill.HITPOINTS) * config.healPercent() / 100);
  }

  @Override
  public void reset() {
    hasStartedRun = false;
  }

  @Override
  public String state() {
    return "RESTORE_HEALTH";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    if (!bank.isOpen()) {
      game.npcs().withName("Eniola").first().interact("Bank");
      game.waitUntil(() -> bank.isOpen());
    }

    bank.withdraw(config.foodId(), 1, false);
    game.waitUntil(() -> game.inventory().withId(config.foodId()).exists());

    // TODO: Will eat all configured food items in the inventory. Probably not an issue.
    while (game.inventory().withId(config.foodId()).exists()) {
      var slot = game.inventory().withId(config.foodId()).first().slot();
      game.interactionManager().interact(9, MenuAction.CC_OP_LOW_PRIORITY.getId(), slot, 983043);

      game.tick();
    }

    hasStartedRun = false;
  }
}
