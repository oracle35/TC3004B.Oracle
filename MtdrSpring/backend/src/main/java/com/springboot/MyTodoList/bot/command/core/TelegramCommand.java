package com.springboot.MyTodoList.bot.command.core;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public abstract class TelegramCommand {
  protected TelegramClient client;
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

  /**
   * Telegram provides a Markdown formatting mode
   * that allows bold, underlined and other styled text.
   * However a new version, Markdown v2, was introduced with
   * more features. It has stricter requirementes and as such
   * some special characters must be escaped. Use this function
   * to escape strings used in MarkdownV2 formatted messages.
   *
   * @param text the text to escape
   */
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

  /**
   * Send a message on chat.
   *
   * @param context The context object provided by `execute` or `executeAuthenticated`.
   * @param processor A lambda that takes in a partially built message and returns a built message.
   * @returns   An Optional<Message> that may be present or not depending on if the request was successful.
   *
   */
  public Optional<Message> sendMessage(CommandContext context, ProcessMessage processor) {
    var partial_msg = SendMessage.builder().chatId(context.getChatId());

    SendMessage msg = processor.process(partial_msg);
    try {
      return Optional.of(this.client.execute(msg));
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  /**
   * Send a message on chat.
   *
   * @param context The context object provided by `execute` or `executeAuthenticated`.
   * @param messageText The text of the message. Format will be plaintext.
   * @returns   An Optional<Message> that may be present or not depending on if the request was successful.
   *
   */
  public Optional<Message> sendMessage(CommandContext context, String messageText) {
    return this.sendMessage(
        context,
        msg -> msg.text(messageText).build());
  }

  /**
   * Send any action supported by telegram's API.
   */
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

  /**
   * Answer a callback query as a toast on the user's screen
   * with a simple text message.
   */
  public void answerCallbackQuery(CallbackQuery callback, String text) {
    try {
      client.execute(AnswerCallbackQuery
          .builder()
          .callbackQueryId(callback.getId())
          .text(text)
          .build());
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  /**
   * Answer a callback query with the provided object.
   */
  public void answerCallbackQuery(AnswerCallbackQuery answer) {
    try {
      client.execute(answer);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method will be executed if the bot receives a callback query
   * that starts with the command name (including a slash, if the command
   * was registered with it).
   *
   * For example, a callback query with the data `task_view_86`
   * will look for a command registered under `task`, but NOT `/task`.
   */
  public void callbackQuery(CallbackQuery query) {
    answerCallbackQuery(query, "Sorry, I don't know how to process this!");
  }

  /**
   * Provides a description for the command to be used
   * for the /help command.
   */
  public abstract String getDescription();

  /**
   * This callback will be executed when a message, split by spaces,
   * starts with the string this command was registered with on the
   * CommandRegistry.
   *
   * @param context The context for this command.
   * @returns A CommandResult. It determines what the processor will do with
   * the next message.
   *
   * @see CommandResult
   * @see CommandContext
   */
  public abstract CommandResult execute(CommandContext context);
}
