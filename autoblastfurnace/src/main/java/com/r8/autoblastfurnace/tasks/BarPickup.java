package com.r8.autoblastfurnace.tasks;

import com.r8.autoblastfurnace.OreType;
import com.r8.autoblastfurnace.Task;
import net.runelite.api.Client;
import net.runelite.client.plugins.iutils.api.GrandExchangePrices;

import javax.inject.Inject;

/**
 * If our current run has not produced any bars (i.e., we are only dropping coal on the conveyor belt) then this step
 * will be ignored. Otherwise, we put on our Ice gloves (if we do not already have them on), and take the bars from the
 * dispenser.
 * TODO: Add support for buckets of water?
 * TODO: Add check that exits out if we do not have ice gloves.
 */
public class BarPickup extends Task {
  @Inject
  protected Client client;

  private boolean hasStartedRun = false;

  @Override
  public boolean validate() {
    return !hopperHasBars() || game.inventory().withId(config.oreType().getBarItemId()).size() > 0;
  }

  @Override
  public void reset() {
    hasStartedRun = false;
  }

  @Override
  public String state() {
    return "BAR_PICKUP";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    // Wear our ice gauntlets if we're not already wearing them.
    if (!game.equipment().withName("Ice gloves").exists()) {
      game.inventory().withName("Ice gloves").first().interact("Wear");
      game.waitUntil(() -> game.equipment().withName("Ice gloves").exists());

      game.sleepDelay();
    }

    game.waitUntil(
        () -> game.objects().withName("Bar dispenser").first().actions().contains("Take"));
    game.objects().withName("Bar dispenser").first().interact("Take");

    game.sleepDelay();

    // "Take" the bars from the furnace in the chat menu.
    // TODO: Check if this works on resizeable mode.
    game.waitUntil(() -> game.widget(270, 14) != null && !game.widget(270, 14).hidden());
    game.widget(270, 14).interact("Take");

    // Handle inventory showing up as empty temporarily. TODO: Fix?
    game.waitChange(() -> game.inventory().withId(config.oreType().getBarItemId()).size());

    int barAmount = game.inventory().withId(config.oreType().getBarItemId()).size();
    int barPrice = GrandExchangePrices.get(config.oreType().getBarItemId()).high;

    plugin.addGoldAmount(barAmount * barPrice);
  }

  private Boolean hopperHasBars() {
    for (var barType : OreType.values()) {
      int barAmount = game.getFromClientThread(() -> client.getVarbitValue(barType.getBarVarBit()));

      if (barAmount > 0) {
        return true;
      }
    }

    return false;
  }
}
