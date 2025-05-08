package com.springboot.MyTodoList.bot.command.core;

import java.util.HashMap;
import java.util.Optional;
import java.util.Map;

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

/**
 * CommandProcessor handles the logic of deciding what
 * commands or actions to execute given a Telegram update.
 */
public class CommandProcessor {
  private final CommandRegistry registry;
  private final TelegramClient client;
  private BotName botName;

  private final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

  private Map<Long, String> currentCommand = new HashMap<>();

  public CommandProcessor(CommandRegistry registry, TelegramClient client) {
    this.registry = registry;
    this.client = client;
    try {
      this.botName = client.execute(new GetMyName());
    } catch (TelegramApiException e) {
      this.botName = new BotName("[unknown]");
    }
  }

  /**
   * Run a command, process its CommandResult and determine
   * the next steps.
   */
  private void runCommand(String[] args, Update update, TelegramCommand cmd, Optional<User> user) {
    Long chatId = update.getMessage().getChatId();
    String commandName = args[0];
    logger.info("Running command " + commandName);
    CommandContext context = new CommandContext(args, update, registry, botName, user);

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
        currentCommand.remove(chatId);
        break;
      case CONTINUE:
        if (!currentCommand.containsKey(chatId)) {
          currentCommand.put(chatId, commandName);
        }
        break;
      case EXECUTE:
        String[] execArgs = result.getExecutedCommand().get();
        logger.info("command wants to execute: /" + execArgs[0]);
        currentCommand.remove(chatId);
        processCommand(execArgs, update, user);
        break;
    }
  }

  private void handleUnknownCommand(String[] args, Update update, Optional<User> user) {
    if (currentCommand != null) {
      Long chatId = update.getMessage().getChatId();
      TelegramCommand cmd =
          registry
              .findCommand(currentCommand.get(chatId))
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "current command in state " + currentCommand + " does not exist."));
      runCommand(args, update, cmd, user);
    }
  } 

  private void processCommand(String[] args, Update update, Optional<User> user) {
    String commandName = args[0];
    registry
        .findCommand(commandName)
        .ifPresentOrElse(
            cmd -> runCommand(args, update, cmd, user),
            () -> handleUnknownCommand(args, update, user));
  }

  /**
   * Called on every update received from Telegram.
   * The main entry point.
   */
  public void processUpdate(Update update, Optional<User> user) {
    if (update.hasMessage() && update.getMessage().hasText()) {
      logger.info("got message: " + update.getMessage().getText());
      String[] args = update.getMessage().getText().split("\\s+");
      processCommand(args, update, user);
    } else if (update.hasCallbackQuery()) {
      String[] args = update.getCallbackQuery().getData().split("_");
      logger.info("callback query for command " + args[0]);
      registry.findCommand(args[0]).ifPresent(cmd -> cmd.callbackQuery(update.getCallbackQuery()));
    }
  }
}
