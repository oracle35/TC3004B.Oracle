package com.springboot.MyTodoList.test.bot.util;

import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.UserAuthenticator;
import com.springboot.MyTodoList.bot.command.core.CommandProcessor;
import com.springboot.MyTodoList.bot.command.core.CommandRegistry;

import static org.mockito.Mockito.*;

/**
 * Test a conversation, simulating full authentication,
 * command state management, and registry.
 * NOTE: Work in progress. Not needed for current sprint but would be
 * nice to have for full integration testing.
 */
public class ConversationTester {
  // private final TelegramClient mockClient;
  // private final CommandRegistry registry;
  // private final UserAuthenticator userAuthenticator;
  // private final CommandProcessor commandProcessor;
  //
  // private final List<SendMessage> thread;
  // private com.springboot.MyTodoList.model.User authUser;
  //
  // private ConversationTester() {
  //   this.mockClient = mock(TelegramClient.class);
  //   this.registry = new CommandRegistry();
  //   this.userAuthenticator = spy(new UserAuthenticator());
  // }
}

