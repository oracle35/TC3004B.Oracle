package com.springboot.MyTodoList.test.bot;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.name.BotName;

import static org.mockito.Mockito.*;

public final class TestFactory {
  public static final User USER = new User(1L, "first", false, "last", "username", null, false, false, false, false, false, false, false);
  public static final BotName BOT_NAME = new BotName("testBot");

  public static Update mockMessageUpdate(User user, String[] args) {
    Update update = mock(Update.class);
    when(update.hasMessage()).thenReturn(true);

    Message message = mock(Message.class);
    when(message.getFrom()).thenReturn(user);
    when(message.hasText()).thenReturn(true);
    when(message.isUserMessage()).thenReturn(true);
    when(message.getText()).thenReturn(String.join(" ", args));
    when(message.getChatId()).thenReturn(user.getId());
    when(update.getMessage()).thenReturn(message);

    return update;
  }
}
