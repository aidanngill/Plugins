package com.r8.autoblastfurnace.tasks;

import com.r8.autoblastfurnace.Task;

/**
 * Deposit bars from our inventory into the bank if we have any.
 */
public class DepositBars extends Task {
  private boolean hasStartedRun = false;

  @Override
  public boolean validate() {
    return game.inventory().withId(config.oreType().getBarItemId()).size() == 0;
  }

  @Override
  public void reset() {
    hasStartedRun = false;
  }

  @Override
  public String state() {
    return "DEPOSIT_BARS";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;
    openBankChest();

    if (game.inventory().withId(config.oreType().getBarItemId()).size() > 0) {
      bank.deposit(config.oreType().getBarItemId(), Integer.MAX_VALUE);
      game.tick(); // TODO: Maybe not necessary?
    }
  }
}
