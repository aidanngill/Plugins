package com.r8.autoblastfurnace.tasks;

import com.r8.autoblastfurnace.Task;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;

import javax.inject.Inject;
import java.util.Random;

/**
 * <p>Fetch any coal or ores from the bank.</p>
 * <p>If we have less than the required amount of coal in the furnace
 * (as shown by the {@code OreType.getCoalNeeded()} variable), then
 * we bring along a full inventory of coal. </p>
 * <p>If this first inventory of coal will bring us over the required
 * amount of coal for this kind of ore, and we have a coal bag to store
 * it in, then we store it in the coal bag and bring the ore along with
 * us. Otherwise, we bring as much coal as we can, whether that be in a
 * coal bag or not.</p>
 */
public class FetchOres extends Task {
  @Inject
  protected Client client;

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
    return "FETCH_ORES";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;
    openBankChest();

    int coalInFurnace =
        game.getFromClientThread(() -> client.getVarbitValue(Varbits.BLAST_FURNACE_COAL));

    // If we don't have enough coal in the furnace, put more in.
    if (config.oreType().needsCoal() && coalInFurnace < config.oreType().getCoalNeeded()) {
      ensureWithdrawal(ItemID.COAL);

      if (hasCoalBag()) {
        bank.close();

        game
            .inventory()
            .withId(ItemID.COAL_BAG)
            .first()
            .interact("Fill");

        game.tick();

        game.objects().withName("Bank chest").nearest().interact("Use");
        game.waitUntil(() -> bank.isOpen());

        // If we have a coal bag, and one inventory of coal will tip us over the edge, bring an inventory of ore too.
        if ((coalInFurnace + game.inventory().withId(ItemID.COAL).size())
            >= config.oreType().getCoalNeeded()) {
          ensureWithdrawal(config.oreType().getOreItemId());
        } else {
          // Otherwise, bring more coal to ensure that we get over the edge.
          ensureWithdrawal(ItemID.COAL);
        }
      }
    } else {
      ensureWithdrawal(config.oreType().getOreItemId());
    }

    game.sleepDelay();

    // 10% chance of actively closing the bank before going to the conveyor belt.
    if (new Random().nextInt(10) == 1) {
      bank.close();
    }

    hasFinishedRun = true;
  }

  private void ensureWithdrawal(int itemId) {
    ensureWithdrawal(itemId, Integer.MAX_VALUE, false);
  }

  private void ensureWithdrawal(int itemId, int quantity, boolean noted) {
    while (game.inventory().withId(itemId).size() == 0) {
      bank.withdraw(itemId, quantity, noted);
      game.tick();
    }
  }
}
