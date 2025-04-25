package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public abstract class TelegramCommand {
  private TelegramClient client;
  private String name;

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

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
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

  public void sendMessage(CommandContext context, String messageText) {
    this.sendMessage(
        context,
        msg -> msg.text(messageText).build());
  }

  public void sendAction(CommandContext context, ActionType actionType) {
    var action = SendChatAction
      .builder()
      .chatId(context.getChatId())
      .action(actionType.toString())
      .build();

    try {
      this.client.execute(action);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  public abstract String getDescription();

  public abstract CommandState execute(CommandContext context);
}
