package com.springboot.MyTodoList.bot.command.core;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public abstract class TelegramCommand {
  private TelegramClient client;
  private String name;

  private final String T_ME_URL = "https://t.me/";

  /**
   * processMessage: a lambda that builds out a message to send.
   */
  public interface ProcessMessage {
    SendMessage process(SendMessageBuilder<?, ?> msg);
  }

  public TelegramCommand(TelegramClient client) {
    this.client = client;
  }

  /*
   * Create a t.me deeplink with that supplies
   * the specified command and arguments separated by colons.
   */
  public String linkCommand(CommandContext context, String... args) {
    String param = Arrays.stream(args).collect(Collectors.joining("_"));
    return 
      T_ME_URL 
        + context.getBotUsername() 
        + "?start="
        + param;
  }

  public String escapeMarkdownV2(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    // Characters that need to be escaped in MarkdownV2
    String[] specialChars = {
      "_", "*", "[", "]", "(", ")", "~", "`",
      ">", "#", "+", "-", "=", "|", "{", "}",
      ".", "!"
    };

    StringBuilder result = new StringBuilder(text);

    // We process the string from right to left to avoid index shifting issues
    // when inserting escape characters
    for (String specialChar : specialChars) {
      int index = result.length() - 1;
      while (index >= 0) {
        if (result.charAt(index) == specialChar.charAt(0)) {
          result.insert(index, '\\');
          index--; // Skip the escaped character
        }
        index--;
      }
    }

    return result.toString();
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

  public abstract CommandResult execute(CommandContext context);
}
