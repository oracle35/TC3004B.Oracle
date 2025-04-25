package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public abstract class TelegramCommand {
    private TelegramClient client;

    enum CommandState {
        CONTINUE,
        FINISH
    }

    /**
     * processMessage: a lambda that builds out a message to send.
     */
    interface ProcessMessage {
        SendMessage process(SendMessageBuilder<?, ?> msg);
    }

    public TelegramCommand(TelegramClient client) {
        this.client = client;
    }

    public void sendMessage(CommandContext context, ProcessMessage processor) {
        var partial_msg = SendMessage
            .builder()
            .chatId(context.getChatId());

        SendMessage msg = processor.process(partial_msg);
        try {
            this.client.execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public abstract String getDescription();
    public abstract CommandState execute(CommandContext context, TelegramClient client);
}
