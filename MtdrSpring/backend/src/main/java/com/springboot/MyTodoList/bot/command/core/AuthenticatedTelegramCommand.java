package com.springboot.MyTodoList.bot.command.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public abstract class AuthenticatedTelegramCommand extends TelegramCommand {
  private Logger logger = LoggerFactory.getLogger(AuthenticatedTelegramCommand.class);

  public AuthenticatedTelegramCommand(TelegramClient client) {
    super(client);
  }

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

  public abstract CommandResult executeAuthenticated(CommandContext context);
}

