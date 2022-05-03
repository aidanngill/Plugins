package com.r8.autozmi;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.QuantityFormatter;

/**
 * Overlay displaying various bits of information about the bot's run, including experience and gold gains.
 */
public class TaskOverlay extends OverlayPanel {
  @Inject
  Client client;

  private final ZmiPlugin plugin;
  private final ZmiConfig config;

  @Inject
  private TaskOverlay(final ZmiPlugin plugin, final ZmiConfig config) {
    super(plugin);

    this.plugin = plugin;
    this.config = config;

    setPosition(OverlayPosition.BOTTOM_LEFT);
    setPriority(OverlayPriority.HIGH);
  }

  @Override
  public Dimension render(Graphics2D graphics) {
    if (!plugin.isProcessRunning()) {
      return null;
    }

    if (!config.isInterfaceEnabled()) {
      return null;
    }

    TableComponent tableComponent = new TableComponent();
    tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

    // Add time spent using the plugin.
    Duration duration = Duration.between(plugin.getStartTime(), Instant.now());
    long seconds = duration.toSeconds();

    String timeString = String.format(
        "%02d:%02d:%02d",
        seconds / 3600,
        (seconds % 3600) / 60,
        (seconds % 60)
    );

    tableComponent.addRow("Time running:", timeString);

    // Add current state of the plugin.
    tableComponent.addRow("State:", plugin.getStateText());

    // Add amount of XP gained.
    int xpGained = client.getSkillExperience(Skill.RUNECRAFT) - plugin.getInitialXpAmount();

    TableComponent xpComponent = new TableComponent();
    xpComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
    xpComponent.addRow("Gained:", QuantityFormatter.quantityToStackSize(xpGained));
    xpComponent.addRow("Per hour:",
        QuantityFormatter.quantityToStackSize(getPerHourRate(seconds, xpGained)));

    // Add GP/hour rate.
    TableComponent goldComponent = new TableComponent();

    goldComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
    goldComponent.addRow("Gained:", QuantityFormatter.quantityToStackSize(plugin.getGoldAmount()));
    goldComponent.addRow("Per hour:",
        QuantityFormatter.quantityToStackSize(getPerHourRate(seconds, plugin.getGoldAmount())));

    if (!tableComponent.isEmpty()) {
      panelComponent.setBackgroundColor(new Color(32, 32, 32, 160));
      panelComponent.setPreferredSize(new Dimension(200, 200));
      panelComponent.setBorder(new Rectangle(5, 5, 5, 5));

      var titleBuilder = TitleComponent.builder()
          .text("Auto ZMI")
          .color(ColorUtil.fromHex("#40c4ff"))
          .build();

      panelComponent.getChildren().add(titleBuilder);
      panelComponent.getChildren().add(tableComponent);

      var xpBuilder = TitleComponent.builder()
          .text("Experience")
          .color(ColorUtil.fromHex("#40c4ff"))
          .build();

      panelComponent.getChildren().add(xpBuilder);
      panelComponent.getChildren().add(xpComponent);

      if (config.isGoldInterfaceEnabled()) {
        var goldBuilder = TitleComponent.builder()
            .text("Gold")
            .color(ColorUtil.fromHex("#40c4ff"))
            .build();

        panelComponent.getChildren().add(goldBuilder);
        panelComponent.getChildren().add(goldComponent);
      }
    }

    return super.render(graphics);
  }

  private Integer getPerHourRate(long seconds, long value) {
    return (int) ((1.0 / (seconds / 3600.0)) * value);
  }
}
