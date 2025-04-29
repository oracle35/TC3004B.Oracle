package com.springboot.MyTodoList.bot.command.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * An AuthenticatedTelegramCommand will show an error message
 * when the CommandContext does not contain a com.springboot.MyTodoList.model.User.
 * The actual authentication is handled by the TelegramBot.
 *
 * Use this class over TelegramCommand when you show anything proceding from the
 * database - such as sprints, tasks, users, etc.
 */
public abstract class AuthenticatedTelegramCommand extends TelegramCommand {
  private Logger logger = LoggerFactory.getLogger(AuthenticatedTelegramCommand.class);

  public AuthenticatedTelegramCommand(TelegramClient client) {
    super(client);
  }

  /**
   * Given a partial message with the necessary context set, such as the
   * chat ID, produce a built SendMessage to be shown when authentication
   * fails.
   */
  public SendMessage getUnauthenticatedMessage(SendMessageBuilder<?,?> partialMsg) {
    String messageText =
      "*Error: not registered\\!*\n" +
      "_I don't know you\\._\n" +
      "If you are a member of our organization" +
      " please contact your manager\\.";
    
    return partialMsg
      .text(messageText)
      .parseMode(ParseMode.MARKDOWNV2)
      .build();
  }

  /**
   * Called when authentication fails.
   */
  public void onAuthFail(CommandContext context) {
    logger.info("auth fail for " + context.getSenderId());
    sendMessage(context, msg -> getUnauthenticatedMessage(msg));
  }

  @Override
  public CommandResult execute(CommandContext context) {
    if (context.getUser().isPresent()) {
      return executeAuthenticated(context);
    } else {
      onAuthFail(context);
      return CommandResult.finish();
    }
  }

  /**
   * Process the command in a secure context. It is guaranteed
   * that the context contains an authenticated user.
   */
  public abstract CommandResult executeAuthenticated(CommandContext context);
}

