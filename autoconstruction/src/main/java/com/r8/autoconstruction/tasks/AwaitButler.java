package com.r8.autoconstruction.tasks;

import com.r8.autoconstruction.Task;
import net.runelite.client.plugins.iutils.ui.Chatbox;

public class AwaitButler extends Task {
  @Override
  public boolean validate() {
    return chatbox.chatState().equals(Chatbox.ChatState.NPC_CHAT);
  }

  @Override
  public String state() {
    return "AWAIT_BUTLER";
  }
}
