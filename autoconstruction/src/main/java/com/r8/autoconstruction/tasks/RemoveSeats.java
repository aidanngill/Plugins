package com.r8.autoconstruction.tasks;

import com.r8.autoconstruction.Task;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.iutils.ui.Chatbox;

@Slf4j
public class RemoveSeats extends Task {
  @Override
  public boolean validate() {
    return game.objects().withName("Seating space").within(1).size() == 2;
  }

  @Override
  public String state() {
    return "REMOVE_SEATS";
  }

  @Override
  public void run() {
    if (hasStartedRun) {
      return;
    }

    hasStartedRun = true;

    game
        .objects()
        .withName("Teak bench")
        .nearestFirst()
        .limit(2)
        .forEach(i -> {
          i.interact("Remove");
          game.waitUntil(() -> chatbox.chatState().equals(Chatbox.ChatState.OPTIONS_CHAT));

          game.sleepDelay();

          doQuickChat(1);
          game.waitUntil(() -> chatbox.chatState().equals(Chatbox.ChatState.CLOSED));
        });
  }
}
