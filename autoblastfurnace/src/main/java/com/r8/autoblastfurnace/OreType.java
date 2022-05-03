package com.r8.autoblastfurnace;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;

/** Different types of ores we can smelt. */
@Getter
public enum OreType {
  GOLD_ORE(
      Varbits.BLAST_FURNACE_GOLD_ORE,
      Varbits.BLAST_FURNACE_GOLD_BAR,
      ItemID.GOLD_ORE, ItemID.GOLD_BAR, 0
  ),

  IRON_ORE(
      Varbits.BLAST_FURNACE_IRON_ORE,
      Varbits.BLAST_FURNACE_STEEL_BAR,
      ItemID.IRON_ORE, ItemID.STEEL_BAR, 26
  ),

  MITHRIL_ORE(
      Varbits.BLAST_FURNACE_MITHRIL_ORE,
      Varbits.BLAST_FURNACE_MITHRIL_BAR,
      ItemID.MITHRIL_ORE, ItemID.MITHRIL_BAR, 52
  ),

  ADAMANTITE_ORE(
      Varbits.BLAST_FURNACE_ADAMANTITE_ORE,
      Varbits.BLAST_FURNACE_ADAMANTITE_BAR,
      ItemID.ADAMANTITE_ORE, ItemID.ADAMANTITE_BAR, 52
  ),

  RUNITE_ORE(
      Varbits.BLAST_FURNACE_RUNITE_ORE,
      Varbits.BLAST_FURNACE_RUNITE_BAR,
      ItemID.RUNITE_ORE, ItemID.RUNITE_BAR, 80
  );

  @Getter
  private final int oreVarBit;

  @Getter
  private final int barVarBit;

  @Getter
  private final int oreItemId;

  @Getter
  private final int barItemId;

  @Getter
  private final int coalNeeded;

  OreType(int oreVarBit, int barVarBit, int oreItemId, int barItemId, int coalNeeded) {
    this.oreVarBit = oreVarBit;
    this.barVarBit = barVarBit;
    this.oreItemId = oreItemId;
    this.barItemId = barItemId;
    this.coalNeeded = coalNeeded;
  }

  public boolean needsCoal() {
    return coalNeeded > 0;
  }
}
