package com.springboot.MyTodoList.bot.command.task;

import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.AuthenticatedTelegramCommand;
import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandResult;

public class TaskViewCommand extends AuthenticatedTelegramCommand {
  public TaskViewCommand(TelegramClient client) {
    super(client);
  }

  @Override
  public String getDescription() {
    return "View the details of a task.";
  }

  @Override
  public CommandResult executeAuthenticated(CommandContext context) {
    sendMessage(context, "You wanted to view task no. " + context.getArguments()[1]);
    return CommandResult.finish();
  }
}

