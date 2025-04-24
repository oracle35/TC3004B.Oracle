package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {
    String getName();

    String getDescription();

    void execute(Update update);
}
