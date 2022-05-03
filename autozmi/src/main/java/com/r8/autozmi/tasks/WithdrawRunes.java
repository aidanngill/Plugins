package com.r8.autozmi.tasks;

import com.r8.autozmi.Task;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;

public class WithdrawRunes extends Task {
  private boolean hasStartedRun = false;
  private boolean hasFinishedRun = false;

  @Getter
  public enum RunePouchInformation {
    SMALL_POUCH("Small pouch", 0, ItemID.SMALL_POUCH),
    MEDIUM_POUCH("Medium pouch", 25, ItemID.MEDIUM_POUCH, ItemID.MEDIUM_POUCH_5511),
    LARGE_POUCH("Large pouch", 50, ItemID.LARGE_POUCH, ItemID.LARGE_POUCH_5513),
    GIANT_POUCH("Giant pouch", 75, ItemID.GIANT_POUCH, ItemID.GIANT_POUCH_5515);

    @Getter
    private final String itemName;

    @Getter
    private final Integer minLevel;

    @Getter
    private final Integer normalItemId;

    @Getter
    @Nullable
    private final Integer degradedItemId;

    RunePouchInformation(String itemName, Integer minLevel, Integer normalItemId) {
      this.itemName = itemName;
      this.minLevel = minLevel;
      this.normalItemId = normalItemId;
      this.degradedItemId = null;
    }

    RunePouchInformation(String itemName, Integer minLevel, Integer normalItemId,
                         @org.jetbrains.annotations.Nullable Integer degradedItemId) {
      this.itemName = itemName;
      this.minLevel = minLevel;
      this.normalItemId = normalItemId;
      this.degradedItemId = degradedItemId;
    }

    public boolean canDegrade() {
      return this.degradedItemId != null;
    }

    public boolean canUse(Integer playerLevel) {
      return playerLevel >= minLevel;
    }

    public static boolean isPouch(Integer itemId) {
      return Arrays.stream(values()).anyMatch(p -> p.normalItemId.equals(itemId));
    }

    public static List<Integer> getNormalItemIds() {
      return Arrays
          .stream(values())
          .map(RunePouchInformation::getNormalItemId)
          .collect(Collectors.toList());
    }
  }

  private final List<Integer> teleportRunes =
      List.of(ItemID.EARTH_RUNE, ItemID.ASTRAL_RUNE, ItemID.LAW_RUNE);

  @Inject
  Client client;

  // TODO: Might cause issues.
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
    return "WITHDRAW_RUNES";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    // If we start with a complete inventory, skip right away.
    if (game.inventory().size() == 28 && game.inventory().withId(ItemID.PURE_ESSENCE).exists()) {
      hasFinishedRun = true;
      return;
    }

    if (!bank.isOpen()) {
      game.npcs().withName("Eniola").first().interact("Bank");
      game.waitUntil(() -> bank.isOpen());
    }

    // Withdraw necessary runes for teleporting.
    // TODO: Add rune pouch support.
    teleportRunes.forEach(i -> bank.withdraw(i, Integer.MAX_VALUE, false));

    // Deposit any other runes.
    game
        .inventory()
        .withNamePart("rune")
        .filter(i -> !teleportRunes.contains(i.id()))
        .forEach(i -> bank.deposit(i.id(), Integer.MAX_VALUE));

    // Withdraw any usable pouches.
    for (var pouch : RunePouchInformation.values()) {

      // Deposit any degraded pouches.
      game
          .inventory()
          .filter(i -> pouch.canDegrade() && i.id() == pouch.getDegradedItemId())
          .forEach(i -> bank.deposit(i.id(), 1));

      // Withdraw any usable pouches we don't already have.
      bank
          .items()
          .stream()
          .filter(i -> i.itemId() == pouch.getNormalItemId())
          .filter(i -> i.quantity() > 0)
          .findFirst()
          .ifPresent(i -> {
            if (pouch.canUse(client.getBoostedSkillLevel(Skill.RUNECRAFT))) {
              bank.withdraw(pouch.getNormalItemId(), 1, false);
              game.sleepDelay();
            }
          });
    }

    // Withdraw pure essence TODO: Other types.
    boolean shouldFillPouches = true;

    // Fill the bags.
    while (game.inventory().size() != 28) {
      bank.withdraw(ItemID.PURE_ESSENCE, Integer.MAX_VALUE, false);
      game.waitChange(() -> game.inventory().size());

      if (shouldFillPouches) {
        var pouchKeySet = game
            .inventory()
            .withId(RunePouchInformation.getNormalItemIds())
            .collect(Collectors.toList());

        for (var pouch : pouchKeySet) {
          game.interactionManager()
              .interact(9, MenuAction.CC_OP_LOW_PRIORITY.getId(), pouch.slot(), 983043);
          game.sleepDelay();
        }

        game.waitChange(() -> game.inventory().size());
      }

      shouldFillPouches = game.inventory().withId(ItemID.PURE_ESSENCE).size() == 0;
    }

    hasFinishedRun = true;
  }
}
