package com.springboot.MyTodoList.bot;

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
import com.springboot.MyTodoList.bot.command.core.StartCommand;
import com.springboot.MyTodoList.bot.command.task.TaskListCommand;
import com.springboot.MyTodoList.service.ToDoItemService;

@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
  private final TelegramClient client;
  private final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
  private final CommandRegistry registry;
  private final CommandProcessor commandProcessor;
  private final String token;
  private final ToDoItemService toDoItemService;

  @Autowired
  public TelegramBot(
      @Value("${telegram.bot.token}") String token, ToDoItemService toDoItemService) {
    this.token = token;
    this.toDoItemService = toDoItemService;
    this.client = new OkHttpTelegramClient(getBotToken());

    this.registry = new CommandRegistry();
    registerCommands();

    this.commandProcessor = new CommandProcessor(registry, client);
  }

  private void registerCommands() {
    this.registry.registerCommand("/start", new StartCommand(client));
    this.registry.registerCommand("/list", new TaskListCommand(client, toDoItemService));
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
    logger.debug("THERE WAS AN UPDATE");
    commandProcessor.processUpdate(update);
  }

  @AfterBotRegistration
  public void afterRegistration(BotSession sesh) {
    logger.info("Bot registered. running: " + sesh.isRunning());
  }
}
