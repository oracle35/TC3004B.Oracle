package com.springboot.MyTodoList.bot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.CommandProcessor;
import com.springboot.MyTodoList.bot.command.core.CommandRegistry;
import com.springboot.MyTodoList.bot.command.core.HelpCommand;
import com.springboot.MyTodoList.bot.command.core.StartCommand;
import com.springboot.MyTodoList.bot.command.core.WhoamiCommand;
import com.springboot.MyTodoList.bot.command.task.NewTaskCommand;
import com.springboot.MyTodoList.bot.command.task.TaskCommand;
import com.springboot.MyTodoList.bot.command.task.TaskListCommand;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.UserService;

@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
  private final TelegramClient client;
  private final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
  private final CommandRegistry registry;
  private final CommandProcessor commandProcessor;
  private final String token;
  private final TaskService taskService;
  private final SprintService sprintService;
  private final UserService userService;

  private Map<Long, Optional<User>> allowedUsers = new HashMap<>();

  @Autowired
  public TelegramBot(
      @Value("${telegram.bot.token}") String token,
      TaskService taskService,
      SprintService sprintService,
      UserService userService) {
    this.token = token;
    this.taskService = taskService;
    this.userService = userService;
    this.sprintService = sprintService;
    this.client = new OkHttpTelegramClient(getBotToken());

    // Build initial cache of existing users
    this.userService.findAll().stream()
        .forEach(
            user -> {
              // Some have no telegram ID yet
              if (user.getID_Telegram() == null) return;
              allowedUsers.put(user.getID_Telegram(), Optional.of(user));
            });

    this.registry = new CommandRegistry();
    registerCommands();

    this.commandProcessor = new CommandProcessor(registry, client);
  }

  private void registerCommands() {
    registry.registerCommand("/start", new StartCommand(client));
    registry.registerCommand("/whoami", new WhoamiCommand(client));
    registry.registerCommand("/help", new HelpCommand(client));
    registry.registerCommand("/tasklist", new TaskListCommand(client, taskService, sprintService));
    registry.registerCommand("/tasknew", new NewTaskCommand(client, taskService));
    registry.registerCommand("task", new TaskCommand(client, taskService, sprintService));
  }

  @Override
  public String getBotToken() {
    return this.token;
  }

  @Override
  public LongPollingUpdateConsumer getUpdatesConsumer() {
    return this;
  }

  /**
   * This function checks that there exists a User on the
   * database with the sender's telegram ID. Its presence
   * determines if the user is authenticated or not.
   */
  private Optional<User> authenticate(Update update) {
    // if (!update.hasMessage()) return Optional.empty();
    // Long senderId = update.getMessage().getFrom().getId();
    Long senderId;

    if (update.hasMessage()) {
      senderId = update.getMessage().getFrom().getId();
    } else if (update.hasCallbackQuery()) {
      senderId = update.getCallbackQuery().getFrom().getId();
    } else {
      logger.error("Unsupported update type!");
      return Optional.empty();
    }

    logger.info("Attempting to authenticate user " + senderId);
    // If the cache contains an entry for this sender use that.
    // If not, compute it in the block defined below
    if (allowedUsers.containsKey(senderId)) {
      return allowedUsers.get(senderId);
    }

    logger.info("Never seem them before. Querying user service...");
    // Query the user service for a user with the sender's telegram id
    Optional<User> queryResult =
        userService.findAll().stream()
            .filter(user -> user.getID_Telegram() == senderId)
            .findFirst();

    // Put the result into the cache and return it
    allowedUsers.put(senderId, queryResult);
    return queryResult;
  }

  /**
   * Method to deny all authentication.
   * used to test authentication failures.
   */
  private Optional<User> authenticate() {
    return Optional.empty();
  }

  @Override
  public void consume(Update update) {
    logger.debug("THERE WAS AN UPDATE");
    commandProcessor.processUpdate(update, authenticate(update));
    // commandProcessor.processUpdate(update, authenticate());
  }

  @AfterBotRegistration
  public void afterRegistration(BotSession sesh) {
    logger.info("Bot registered. running: " + sesh.isRunning());
  }
}
