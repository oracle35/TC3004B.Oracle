package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public class CommandContext {
    private final Update update;
    private final Message message;

    public CommandContext(Update update) {
        this.update = update;
        this.message = update.getMessage();
    }

    Message getMessage() {
        return this.message;
    }

    Long getChatId() {
        return this.message.getChatId();
    }

    Long getSender() {
        return this.message.getFrom().getId();
    }
}
