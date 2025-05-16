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

  public static Update mockMessageUpdate(User user, String msg) {
    Update update = mock(Update.class);

    Message message = mock(Message.class);
    // false positive unused stubbing warning
    lenient().when(message.getText()).thenReturn(msg);
    when(update.getMessage()).thenReturn(message);
    when(update.hasMessage()).thenReturn(true);
    when(message.getChatId()).thenReturn(user.getId());

    return update;
  }
}
