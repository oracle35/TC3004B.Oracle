package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public abstract class TelegramCommand {
  private TelegramClient client;

  /**
   * Enum to manage the CommandProcessor's state machine.
   */
  public enum CommandState {
    /*
     * The next message will be routed to this command.
     */
    CONTINUE,
    /*
     * Tell the CommandProcessor to start handling other
     * commands on the next message.
     */
    FINISH,
  }

  /**
   * processMessage: a lambda that builds out a message to send.
   */
  public interface ProcessMessage {
    SendMessage process(SendMessageBuilder<?, ?> msg);
  }

  public TelegramCommand(TelegramClient client) {
    this.client = client;
  }

  public void sendMessage(CommandContext context, ProcessMessage processor) {
    var partial_msg = SendMessage.builder().chatId(context.getChatId());

    SendMessage msg = processor.process(partial_msg);
    try {
      this.client.execute(msg);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  public void sendAction(ActionType action) {}

  public abstract String getDescription();

  public abstract CommandState execute(CommandContext context, TelegramClient client);
}
