package com.r8.autolog;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("autolog")
public interface AutoLogConfig extends Config {
  @ConfigSection(
      keyName = "autoLogTitle",
      name = "Conditions",
      description = "Which conditions we should log out for",
      position = 1
  )
  String autoLogTitle = "Conditions";

  @ConfigItem(
      keyName = "wildernessOnly",
      name = "Wilderness Only",
      description = "Only log out when we are in the wilderness",
      position = 2,
      section = autoLogTitle
  )
  default boolean wildernessOnly() {
    return true;
  }

  @ConfigItem(
      keyName = "wildernessLevelOnly",
      name = "Wilderness Level Range Only",
      description = "Only log out for players who are within our combat range in the Wilderness",
      position = 3,
      section = autoLogTitle
  )
  default boolean wildernessLevelOnly() {
    return true;
  }

  @ConfigItem(
      keyName = "skulledOnly",
      name = "Skulled Only",
      description = "Only log out when a skulled player is seen",
      position = 3,
      section = autoLogTitle
  )
  default boolean skulledOnly() {
    return true;
  }

  @Range(
      max = 28
  )
  @ConfigItem(
      keyName = "inventoryThreshold",
      name = "Inventory Threshold",
      description = "Minimum amount of items to log out for",
      position = 4,
      section = autoLogTitle
  )
  default int inventoryThreshold() {
    return 4;
  }
}
