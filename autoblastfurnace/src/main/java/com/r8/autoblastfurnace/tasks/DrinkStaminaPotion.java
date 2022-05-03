package com.r8.autoblastfurnace.tasks;

import com.r8.autoblastfurnace.Task;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.client.plugins.iutils.api.GrandExchangePrices;

import javax.inject.Inject;

/** Drink a dose of a stamina potion, so long as we have less
 * than the configured run energy with no active stamina potion
 * effect. */
public class DrinkStaminaPotion extends Task {
  @Inject
  protected Client client;

  private boolean hasStartedRun = false;

  @Override
  public boolean validate() {
    // No longer need to do the task if we are using a stamina potion, or our run energy regenerated to acceptable levels.
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
    openBankChest();

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

      bank.close();

      game.waitUntil(() -> game.inventory().size() > 0);
      game.inventory().withNamePart("stamina").first().interact("Drink");

      // Remove the value of one stamina potion sip from the GP earned.
      plugin.addGoldAmount(-GrandExchangePrices.get(12631).high);

      game.sleepDelay();

      game.objects().withName("Bank chest").nearest().interact("Use");
      game.waitUntil(() -> bank.isOpen());
    }
  }

  private boolean hasStaminaEffect() {
    return game.getFromClientThread(
        () -> client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE)) == 1;
  }
}
