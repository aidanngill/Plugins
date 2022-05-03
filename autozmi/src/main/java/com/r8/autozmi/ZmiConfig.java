package com.r8.autozmi;

import net.runelite.api.ItemID;
import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("autozmi")
public interface ZmiConfig extends Config {
  @ConfigSection(
      keyName = "userInterfaceSection",
      name = "User Interface",
      description = "Adjust how the UI overlay displays on the screen",
      position = 1
  )
  String userInterfaceSection = "User Interface";

  @ConfigItem(
      keyName = "isInterfaceEnabled",
      name = "Enabled",
      description = "Enable the UI overlay when the bot is running",
      position = 2,
      section = userInterfaceSection
  )
  default boolean isInterfaceEnabled() {
    return true;
  }

  @ConfigItem(
      keyName = "isGoldInterfaceEnabled",
      name = "Display GP statistics",
      description = "Whether to display total GP spent and GP/hour rates in the overlay",
      position = 3,
      section = userInterfaceSection
  )
  default boolean isGoldInterfaceEnabled() {
    return true;
  }

  @ConfigSection(
      keyName = "miscSection",
      name = "Miscellaneous",
      description = "Uncategorized features",
      position = 4
  )
  String miscSection = "Miscellaneous";

  @Range(max = 100)
  @ConfigItem(
      keyName = "minRunEnergy",
      name = "Min run energy",
      description = "Minimum amount of run energy to have before sipping a stamina potion",
      position = 5,
      section = miscSection
  )
  default int minRunEnergy() {
    return 30;
  }

  @ConfigItem(
      keyName = "foodId",
      name = "Food ID",
      description = "What food to restore hitpoints with",
      position = 6,
      section = miscSection
  )
  default int foodId() {
    return ItemID.POTATO_WITH_CHEESE;
  }

  @Range(max = 100)
  @ConfigItem(
      keyName = "healPercent",
      name = "Heal percent",
      description = "To what percent of your hitpoints to heal towards",
      position = 7,
      section = miscSection
  )
  default int healPercent() {
    return 75;
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
