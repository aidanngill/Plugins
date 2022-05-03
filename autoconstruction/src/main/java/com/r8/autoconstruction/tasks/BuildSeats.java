package com.r8.autoconstruction.tasks;

import com.r8.autoconstruction.Task;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.iutils.api.GrandExchangePrices;

public class BuildSeats extends Task {
  @Override
  public boolean validate() {
    return game
        .objects()
        .withName("Teak bench")
        .within(1)
        .size() == 2;
  }

  @Override
  public String state() {
    return "BUILD_SEATS";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    game
        .objects()
        .withName("Seating space")
        .nearestFirst()
        .limit(2)
        .forEach(i -> {
          // Select the "Build" option on the game object itself.
          while (game.widget(458, 4) == null) {
            i.interact("Build");
            game.tick();
          }

          game.sleepDelay();

          // Select the "Build" option from the widget.
          while (game.widget(458, 4) != null) {
            game.widget(458, 4).interact("Build");
            game.tick(2);
          }

          plugin.addGoldAmount(-(4 * GrandExchangePrices.get(ItemID.TEAK_PLANK).high));

          game.sleepDelay();
        });
  }
}
