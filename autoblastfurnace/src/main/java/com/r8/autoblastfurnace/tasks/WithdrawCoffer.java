package com.r8.autoblastfurnace.tasks;

import com.r8.autoblastfurnace.ReflectBreakHandler;
import com.r8.autoblastfurnace.Task;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.iutils.ui.Chatbox;

import javax.inject.Inject;

/**
 * Withdraw any coins we have from the coffer if we are on a break,
 * or attempting to go on a break at least. The coins will stay in
 * the player's inventory.
 */
public class WithdrawCoffer extends Task {
  @Inject
  protected ReflectBreakHandler reflectBreakHandler;

  private boolean hasStartedRun = false;

  @Override
  public boolean validate() {
    if (reflectBreakHandler.isBreakActive(plugin) || reflectBreakHandler.shouldBreak(plugin)) {
      return plugin.getCofferAmount().equals(0);
    }

    return true;
  }

  @Override
  public void reset() {
    hasStartedRun = false;
  }

  @Override
  public String state() {
    return "COFFER_WITHDRAW";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    if (game.inventory().size() == 28) {
      if (!bank.isOpen()) {
        game.objects().withName("Bank chest").nearest().interact("Use");
        game.waitUntil(() -> bank.isOpen());
      }

      bank.deposit(ItemID.COAL, Integer.MAX_VALUE);

      bank.deposit(config.oreType().getOreItemId(), Integer.MAX_VALUE);
      bank.deposit(config.oreType().getBarItemId(), Integer.MAX_VALUE);

      game.tick();
      bank.close();
    }

    game.objects().withName("Coffer").first().interact("Use");
    game.waitUntil(() -> chatbox.chatState().equals(Chatbox.ChatState.OPTIONS_CHAT));

    chatbox.chat("Withdraw");
    game.chooseNumber(plugin.getCofferAmount());

    game.tick();

    chatbox.continueChat();
  }
}
