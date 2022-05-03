package com.r8.autozmi.tasks;

import com.google.inject.Inject;
import com.r8.autozmi.Task;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Varbits;

public class RestoreStamina extends Task {
  @Inject
  Client client;

  private boolean hasStartedRun = false;

  @Override
  public boolean validate() {
    return hasStaminaEffect() || game.energy() >= config.minRunEnergy();
  }

  @Override
  public void reset() {
    hasStartedRun = false;
  }

  @Override
  public String state() {
    return "RESTORE_STAMINA";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    game.npcs().withName("Eniola").first().interact("Bank");
    game.waitUntil(() -> bank.isOpen());

    // MenuOption=Eat MenuTarget=<col=ff9040>Potato with cheese</col> Id=9 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=11 Param1=983043 CanvasX=630 CanvasY=434
    // MenuOption=Drink MenuTarget=<col=ff9040>Stamina potion(1)</col> Id=9 Opcode=CC_OP_LOW_PRIORITY/1007 Param0=2 Param1=983043 CanvasX=612 CanvasY=363

    var staminaPotionItem = bank
        .items()
        .stream()
        .filter(i -> i.name().equalsIgnoreCase("stamina potion(1)"))
        .filter(i -> i.quantity() > 0)
        .findFirst();

    if (staminaPotionItem.isPresent()) {
      while (!game.inventory().withId(staminaPotionItem.get().itemId()).exists()) {
        bank.withdraw(staminaPotionItem.get().itemId(), 1, false);
        game.waitUntil(() -> game.inventory().withId(staminaPotionItem.get().itemId()).exists(), 2);
      }

      var slot = game.inventory().withId(staminaPotionItem.get().itemId()).first().slot();
      game.interactionManager().interact(9, MenuAction.CC_OP_LOW_PRIORITY.getId(), slot, 983043);
    }
  }

  private boolean hasStaminaEffect() {
    return game.getFromClientThread(
        () -> client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE)) == 1;
  }
}
