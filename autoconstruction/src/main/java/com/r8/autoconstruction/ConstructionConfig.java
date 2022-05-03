package com.r8.autoconstruction;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("autoconstruction")
public interface ConstructionConfig extends Config {
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

  @ConfigItem(
      keyName = "startButton",
      name = "Start/stop",
      description = "Run the bot",
      position = 4
  )
  default Button startButton() {
    return new Button();
  }
}
