package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public class CommandContext {
    private final Update update;
    private final Message message;
    private boolean shouldFinish;

    CommandContext(Update update) {
        this.update = update;
        this.message = update.getMessage();
    }

    void FinishCommand() {
        this.shouldFinish = true;
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
