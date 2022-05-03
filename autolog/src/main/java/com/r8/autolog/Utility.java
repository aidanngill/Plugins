package com.r8.autolog;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.api.events.MenuOptionClicked;

@Slf4j
@Singleton
public class Utility {
  @Inject
  private Client client;

  @Inject
  private ExecutorService executorService;

  public void menuAction(MenuOptionClicked menuOptionClicked, String option, String target,
                         int identifier, MenuAction menuAction, int param0, int param1) {
    menuOptionClicked.setMenuOption(option);
    menuOptionClicked.setMenuTarget(target);
    menuOptionClicked.setId(identifier);
    menuOptionClicked.setMenuAction(menuAction);
    menuOptionClicked.setActionParam(param0);
    menuOptionClicked.setWidgetId(param1);
  }

  public void mouseClick() {
    executorService.submit(() ->
    {
      Point point = new Point(0, 0);

      mouseEvent(MouseEvent.MOUSE_ENTERED, point);
      mouseEvent(MouseEvent.MOUSE_EXITED, point);
      mouseEvent(MouseEvent.MOUSE_MOVED, point);

      mouseEvent(MouseEvent.MOUSE_PRESSED, point);
      mouseEvent(MouseEvent.MOUSE_RELEASED, point);
      mouseEvent(MouseEvent.MOUSE_CLICKED, point);
    });
  }

  public void mouseEvent(int id, Point point) {
    MouseEvent mouseEvent = new MouseEvent(
        client.getCanvas(), id,
        System.currentTimeMillis(),
        0, point.getX(), point.getY(),
        1, false, 1
    );

    client.getCanvas().dispatchEvent(mouseEvent);
  }

  @SuppressWarnings("SameParameterValue")
  public void sendKey(int key) {
    keyEvent(KeyEvent.KEY_PRESSED, key);
    keyEvent(KeyEvent.KEY_RELEASED, key);
  }

  public void keyEvent(int id, int key) {
    KeyEvent e = new KeyEvent(
        client.getCanvas(), id, System.currentTimeMillis(),
        0, key, KeyEvent.CHAR_UNDEFINED
    );

    client.getCanvas().dispatchEvent(e);
  }
}