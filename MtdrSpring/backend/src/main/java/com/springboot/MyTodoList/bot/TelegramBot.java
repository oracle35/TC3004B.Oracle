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
  private final UserAuthenticator userAuthenticator;

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
    this.userAuthenticator = new UserAuthenticator(userService);
    this.client = new OkHttpTelegramClient(getBotToken());

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

  @Override
  public void consume(Update update) {
    Long senderId;
    if (update.hasMessage()) {
      senderId = update.getMessage().getFrom().getId();
    } else if (update.hasCallbackQuery()) {
      senderId = update.getCallbackQuery().getFrom().getId();
    } else {
      logger.error("Unsupported update type!");
      return;
    }

    commandProcessor.processUpdate(update, userAuthenticator.authenticate(senderId));
  }

  @AfterBotRegistration
  public void afterRegistration(BotSession sesh) {
    logger.info("Bot registered. running: " + sesh.isRunning());
  }
}
