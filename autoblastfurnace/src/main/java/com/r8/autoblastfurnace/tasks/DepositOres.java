package com.r8.autoblastfurnace.tasks;

import com.r8.autoblastfurnace.Task;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.iutils.api.GrandExchangePrices;

/** Deposit the ores we got from the bank onto the conveyor belt. If we have
 * a coal bag in our inventory we empty it and put any coal we get onto the
 * conveyor belt as well. */
public class DepositOres extends Task {
  private boolean hasStartedRun = false;
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
    return "DEPOSIT_ORES";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    var totalItemPrice = game
        .inventory()
        .withId(ItemID.COAL, config.oreType().getOreItemId())
        .map(i -> GrandExchangePrices.get(i.id()).high)
        .reduce(0, Integer::sum);

    game.objects().withId(9100).first().interact("Put-ore-on");

    // Make extra sure that our inventory is actually loaded. TODO: Fix?
    game.waitUntil(() -> game.inventory().size() > 0);

    // Wait until we deposit.
    game.waitChange(() -> game.inventory().size());

    plugin.addGoldAmount(-totalItemPrice);

    // If we have a coal bag, empty it and put what we have there too.
    if (config.oreType().needsCoal() && hasCoalBag()) {
      game.inventory().withName("Coal bag").first().interact("Empty");
      game.waitChange(() -> game.inventory().size()); // TODO: Maybe not necessary?

      var coalPrice =
          game.inventory().withId(ItemID.COAL).size() * GrandExchangePrices.get(ItemID.COAL).high;

      game.objects().withId(9100).first().interact("Put-ore-on");
      game.waitChange(() -> game.inventory().size());

      plugin.addGoldAmount(-coalPrice);
    }

    hasFinishedRun = true;
  }
}
