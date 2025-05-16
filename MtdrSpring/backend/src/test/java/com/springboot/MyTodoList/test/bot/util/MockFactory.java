package com.springboot.MyTodoList.test.bot.util;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.name.BotName;

import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandRegistry;

import static org.mockito.Mockito.*;

import java.util.Optional;

public final class MockFactory {
  public static final User USER = new User(1L, "first", false, "last", "username", null, false, false, false, false, false, false, false);
  public static final BotName BOT_NAME = new BotName("testBot");

  public static Update mockMessageUpdate(User user, String[] args) {
    Update update = mock(Update.class);
    when(update.hasMessage()).thenReturn(true);

    Message message = mock(Message.class);
    when(message.getChatId()).thenReturn(user.getId());
    when(update.getMessage()).thenReturn(message);
    when(update.hasMessage()).thenReturn(true);

    return update;
  }

  public static CommandContext contextForMessage(String msg, com.springboot.MyTodoList.model.User user) {
    String[] args = msg.split(" ");
    Update update = mockMessageUpdate(USER, args);
    CommandRegistry registry = mock(CommandRegistry.class);
    return new CommandContext(args, update, registry, BOT_NAME, Optional.of(user));
  }
}
