package com.r8.autozmi.tasks;

import com.r8.autozmi.Task;
import java.util.stream.Collectors;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.iutils.api.GrandExchangePrices;
import net.runelite.client.plugins.iutils.scene.Position;
import net.runelite.client.plugins.iutils.ui.Prayer;

/**
 * Run to the altar, enable prayer against mage.
 */
public class CraftRunes extends Task {
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
    return "CRAFT_RUNES";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    // TODO: Add prayer level check.
    prayers.setEnabled(Prayer.PIETY, true);
    prayers.setEnabled(Prayer.PROTECT_FROM_MAGIC, true);

    walking.walkTo(new Position(3058, 5579, 0));

    // Get initial item worth before crafting.
    var originalItemWorth = game
        .inventory()
        .filter(i -> i.name().endsWith("rune"))
        .map(i -> GrandExchangePrices.get(i.id()).high * i.quantity())
        .reduce(0, Integer::sum);

    game.objects().withName("Altar").first().interact("Craft-rune");
    game.waitUntil(() -> game.inventory().withId(ItemID.PURE_ESSENCE).size() == 0);

    // TODO: Keep repeating til we don't get any more?
    var pouchList = game
        .inventory()
        .withNamePart("pouch")
        .collect(Collectors.toList());

    for (var pouch : pouchList) {
      pouch.interact("Empty");
      game.sleepDelay();
    }

    game.objects().withName("Altar").first().interact("Craft-rune");
    game.tick();

    var afterItemWorth = game
        .inventory()
        .filter(i -> i.name().endsWith("rune"))
        .map(i -> GrandExchangePrices.get(i.id()).high * i.quantity())
        .reduce(0, Integer::sum);

    plugin.addGoldAmount(afterItemWorth - originalItemWorth);
    game.randomDelay();

    hasFinishedRun = true;
  }
}
