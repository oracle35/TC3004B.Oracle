package com.springboot.MyTodoList.test.bot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.UserAuthenticator;
import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandProcessor;
import com.springboot.MyTodoList.bot.command.core.CommandRegistry;
import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.bot.command.core.TelegramCommand;
import com.springboot.MyTodoList.model.User;

import static org.mockito.Mockito.*;

public class CommandTester {
  private final TelegramClient mockClient;
  private final CommandRegistry registry;
  private TelegramCommand command;

  private Optional<User> appUser = Optional.empty();
  private final List<SendMessage> response = new ArrayList<>();
  private com.springboot.MyTodoList.model.User authUser;

  private final Logger logger = LoggerFactory.getLogger(CommandTester.class);

  public interface CommandProvider {
    public TelegramCommand getCommand(TelegramClient client); 
  }

  private CommandTester() {
    this.mockClient = mock(TelegramClient.class);
    this.registry = new CommandRegistry();
    try {
      when(mockClient.execute(any(SendMessage.class)))
        .thenAnswer(invocation -> {
          SendMessage message = invocation.getArgument(0);
          response.add(message);

          Message mockMessage = mock(Message.class);
          return mockMessage;
        });
    } catch (TelegramApiException e) {
      logger.error("Unreachable code executed: TelegramApiException in mockito mock");
    }
  }

  public static CommandTester create() {
    return new CommandTester();
  }

  public CommandTester withCommand(String name, CommandProvider provider) {
    command = provider.getCommand(mockClient);
    registry.registerCommand(name, command);
    return this;
  }

  public CommandTester withAuthentication() {
    // Mock an authenticated user
    User appUser = new User();
    appUser.setID_User(1);
    this.appUser = Optional.of(appUser);
    return this;
  }

  public BotResponse sendMessage(String msg) {
    response.clear();
    String[] args = msg.split(" ");

    Update update = MockFactory.mockMessageUpdate(MockFactory.USER, msg);
    CommandContext context = new CommandContext(args, update, registry, MockFactory.BOT_NAME, appUser);

    CommandResult result = command.execute(context);
    return new BotResponse(response, result);
  }
}

