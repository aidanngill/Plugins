package com.r8.autoblastfurnace;

import net.runelite.client.plugins.iutils.scripts.UtilsScript;

import javax.inject.Inject;

/** Single task to be run. */
public abstract class Task extends UtilsScript {
  @Inject
  protected BlastFurnacePlugin plugin;

  @Inject
  protected BlastFurnaceConfig config;

  public Task() {}

  /** Check if the conditions for finishing the task are met. */
  public abstract boolean validate();

  /** Reset any values that were stored in the task class. */
  public abstract void reset();

  // TODO: Maybe make a human readable state?
  /** Get what task is being done in a string form. */
  public abstract String state();

  /** Will be run on every game tick, so long as it is the current task in the set. */
  public abstract void run();

  /** Whether or not the user has the Coal bag item. */
  protected boolean hasCoalBag() {
    return game.inventory().withName("Coal bag").size() > 0;
  }

  /** Open the bank chest in the Blast Furnace, as {@code bank()} will fail to find it. */
  protected void openBankChest() {
    if (!bank.isOpen()) {
      game.objects().withName("Bank chest").nearest().interact("Use");
      game.waitUntil(() -> bank.isOpen());
    }
  }
}
