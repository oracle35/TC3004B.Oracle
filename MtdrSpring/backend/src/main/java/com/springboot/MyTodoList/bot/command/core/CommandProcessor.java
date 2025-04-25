package com.springboot.MyTodoList.bot.command.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.TelegramCommand.CommandState;

public class CommandProcessor {
    private final CommandRegistry registry;
    private final TelegramClient client;

    private final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    private String currentCommand = null;

    public CommandProcessor(CommandRegistry registry, TelegramClient client) {
        this.registry = registry;
        this.client = client;
    }

    public void runCommand(String commandName, Update update, TelegramCommand cmd) {
        logger.info("Running command " + commandName);
        CommandContext context = new CommandContext(update);
       
        // Start typing
        SendChatAction action = SendChatAction
            .builder()
            .chatId(update.getMessage().getChatId())
            .action(ActionType.TYPING.toString())
            .build();
        try {
            client.execute(action);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        CommandState state = cmd.execute(context, client);
        switch(state) {
            case FINISH:
                currentCommand = null;
                break;
            case CONTINUE:
                currentCommand = commandName;
                break;
        }
    };

    public void processUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            logger.info("got message: " + update.getMessage().getText());
            String messageText = update.getMessage().getText();
            String commandName = messageText.split("\\s+")[0];
            registry.findCommand(commandName).ifPresentOrElse(
                cmd -> runCommand(commandName, update, cmd),
                () -> {
                    if (currentCommand != null) {
                        TelegramCommand cmd = registry.findCommand(commandName).orElseThrow(
                            () -> new IllegalStateException("current command in state " + currentCommand + " does not exist.")
                        );
                        runCommand(commandName, update, cmd);
                    } 
                }
            );
        }
    }
}
