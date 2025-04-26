package com.springboot.MyTodoList.bot.command.core;

import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.name.GetMyName;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.name.BotName;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.model.User;

public class CommandProcessor {
  private final CommandRegistry registry;
  private final TelegramClient client;
  private BotName botName;

  private final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

  private String currentCommand = null;

  public CommandProcessor(CommandRegistry registry, TelegramClient client) {
    this.registry = registry;
    this.client = client;
    try {
      this.botName = client.execute(new GetMyName());
    } catch (TelegramApiException e) {
      this.botName = new BotName("[unknown]");
    }
  }

  public void runCommand(String commandName, Update update, TelegramCommand cmd, Optional<User> user) {
    logger.info("Running command " + commandName);
    CommandContext context = new CommandContext(update, registry, botName, user);

    // Start typing
    SendChatAction action =
        SendChatAction.builder()
            .chatId(update.getMessage().getChatId())
            .action(ActionType.TYPING.toString())
            .build();
    try {
      client.execute(action);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }

    CommandResult result = cmd.execute(context);
    switch (result.getState()) {
      case FINISH:
        currentCommand = null;
        break;
      case CONTINUE:
        if (currentCommand == null) {
          currentCommand = commandName;
        }
        break;
      case EXECUTE:
        throw new NotImplementedException();
    }
  }

  private void handleUnknownCommand(String commandName, Update update, Optional<User> user) {
    if (currentCommand != null) {
      TelegramCommand cmd =
          registry
              .findCommand(currentCommand)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "current command in state " + currentCommand + " does not exist."));
      runCommand(commandName, update, cmd, user);
    }
  }

  public void processUpdate(Update update, Optional<User> user) {
    logger.info("update");
    if (update.hasMessage() && update.getMessage().hasText()) {
      logger.info("got message: " + update.getMessage().getText());
      String messageText = update.getMessage().getText();
      String commandName = messageText.split("\\s+")[0];
      registry
          .findCommand(commandName)
          .ifPresentOrElse(
              cmd -> runCommand(commandName, update, cmd, user),
              () -> handleUnknownCommand(commandName, update, user));
    }
  }
}
