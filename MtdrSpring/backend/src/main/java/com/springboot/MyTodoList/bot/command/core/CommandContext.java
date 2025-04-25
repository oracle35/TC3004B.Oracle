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

    public Message getMessage() {
        return this.message;
    }

    public String[] getArguments() {
        //TODO: this assumes the message has text. Will it always?
        return this.message.getText().split("\\s+");
    }

    public Long getChatId() {
        return this.message.getChatId();
    }

    public Long getSender() {
        return this.message.getFrom().getId();
    }
}
