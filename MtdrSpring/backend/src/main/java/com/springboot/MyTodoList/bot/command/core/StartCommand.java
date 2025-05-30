package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class StartCommand extends AuthenticatedTelegramCommand {
  public StartCommand(TelegramClient client) {
    super(client);
  }

  @Override
  public String getDescription() {
    return "Start the bot";
  }

  @Override
  public CommandResult executeAuthenticated(CommandContext context) {
    if (context.hasArguments()) {
      String[] args = context.getArguments()[1].split("_");
      return CommandResult.execute(args);
    }

    sendMessage(
        context,
        msg ->
            msg.text("Hello! I'm the TodoList bot. Type /helpto see avalable commands!")
                .build());
    return CommandResult.finish();
  }
}
