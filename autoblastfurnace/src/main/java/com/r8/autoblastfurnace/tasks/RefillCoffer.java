package com.r8.autoblastfurnace.tasks;

import com.r8.autoblastfurnace.Task;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.iutils.ui.Chatbox;

import java.time.Duration;
import java.time.Instant;

/**
 * Refill the coffer if it is below the necessary amount.
 */
@Slf4j
public class RefillCoffer extends Task {
  private boolean hasStartedRun = false;

  @Override
  public boolean validate() {
    return plugin.getCofferAmount() > getNecessaryAmount();
  }

  @Override
  public void reset() {
    hasStartedRun = false;
  }

  @Override
  public String state() {
    return "COFFER_REFILL";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;
    openBankChest();

    int initialCofferAmount = plugin.getCofferAmount();

    // Withdraw enough for the break + 1k to be safe.
    int goldToWithdraw = getNecessaryAmount() - initialCofferAmount + 1_000;

    if (goldToWithdraw <= 0) {
      return;
    }

    // Deposit any excess gold we may already have to make things easier.
    bank.deposit(ItemID.COINS_995, Integer.MAX_VALUE);
    game.tick();

    log.info("Putting {} GP into the coffer", goldToWithdraw);

    bank.withdraw(ItemID.COINS_995, goldToWithdraw, false);
    game.tick();

    bank.close();

    while (plugin.getCofferAmount() == 0) {
      game.objects().withName("Coffer").first().interact("Use");
      game.waitUntil(() -> chatbox.chatState().equals(Chatbox.ChatState.OPTIONS_CHAT));

      // Deposit coins.
      chatbox.chat("Deposit");

      game.chooseNumber(goldToWithdraw);
      game.waitUntil(() -> plugin.getCofferAmount() > initialCofferAmount, 3);
    }
  }

  /**
   * Get the amount of gold needed to fill the coffer before a break starts.
   */
  public Integer getNecessaryAmount() {
    Instant nextBreak = plugin.getPlannedBreak();
    Duration timeDelta = Duration.between(Instant.now(), nextBreak);

    return getCofferAmountNeeded(timeDelta);
  }

  /**
   * Get the amount of gold needed to fill the coffer for a certain period of time.
   */
  public Integer getCofferAmountNeeded(Duration timeDuration) {
    return (int) Math.ceil((72_000.f / 3600.f) * timeDuration.getSeconds());
  }
}
