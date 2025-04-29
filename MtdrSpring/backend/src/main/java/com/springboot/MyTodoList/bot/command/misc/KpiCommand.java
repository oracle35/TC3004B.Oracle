package com.springboot.MyTodoList.bot.command.misc;

import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.AuthenticatedTelegramCommand;
import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandResult;

public class KpiCommand extends AuthenticatedTelegramCommand {

    public KpiCommand(TelegramClient client) {
        super(client);
    }

    @Override
    public String getDescription() {
        return "Show your key KPIs and stats for this sprint.";
    }

    @Override
    public CommandResult executeAuthenticated(CommandContext context) {
        return CommandResult.finish();
    }
}

