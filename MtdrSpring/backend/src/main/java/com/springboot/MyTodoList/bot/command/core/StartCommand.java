package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class StartCommand extends TelegramCommand {
    public StartCommand(TelegramClient client) {
        super(client);
    }

    @Override
    public String getDescription() {
        return "Start the bot";
    }

    @Override
    public CommandState execute(CommandContext context, TelegramClient client) {
        sendMessage(
            context, 
            msg -> msg
                .text("Welcome to the TodoList bot! Nothing's implemented yet, but be prepared!!!!")
                .build()
        );
        return CommandState.FINISH;
    }
}

