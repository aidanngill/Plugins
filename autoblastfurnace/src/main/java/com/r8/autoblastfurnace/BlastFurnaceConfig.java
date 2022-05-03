package com.r8.autoblastfurnace;

import net.runelite.client.config.*;

@ConfigGroup("autoblastfurnace")
public interface BlastFurnaceConfig extends Config {
  @ConfigSection(
      keyName = "smeltingSection",
      name = "Smelting",
      description = "What ores to smelt",
      position = 1
  )
  String smeltingSection = "Smelting";

  @ConfigItem(
      keyName = "oreType",
      name = "Ore type",
      description = "What kind of ore to smelt",
      position = 2,
      section = smeltingSection
  )
  default OreType oreType() {
    return OreType.GOLD_ORE;
  }

  @ConfigSection(
      keyName = "userInterfaceSection",
      name = "User Interface",
      description = "Adjust how the UI overlay displays on the screen",
      position = 3
  )
  String userInterfaceSection = "User Interface";

  @ConfigItem(
      keyName = "isInterfaceEnabled",
      name = "Enabled",
      description = "Enable the UI overlay when the bot is running",
      position = 4,
      section = userInterfaceSection
  )
  default boolean isInterfaceEnabled() {
    return true;
  }

  @ConfigItem(
      keyName = "isGoldInterfaceEnabled",
      name = "Display GP statistics",
      description = "Whether to display total GP spent and GP/hour rates in the overlay",
      position = 5,
      section = userInterfaceSection
  )
  default boolean isGoldInterfaceEnabled() {
    return true;
  }

  @ConfigSection(
      keyName = "miscSection",
      name = "Miscellaneous",
      description = "Uncategorized features",
      position = 6
  )
  String miscSection = "Miscellaneous";

  @Range(max = 100)
  @ConfigItem(
      keyName = "minRunEnergy",
      name = "Min run energy",
      description = "Minimum amount of run energy to have before sipping a stamina potion",
      position = 7,
      section = miscSection
  )
  default int minRunEnergy() {
    return 30;
  }

  @ConfigItem(
      keyName = "startButton",
      name = "Start/stop",
      description = "Run the bot",
      position = 8
  )
  default Button startButton() {
    return new Button();
  }
}
