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
            msg.text(
                    "Welcome to the TodoList bot! Nothing's implemented yet,"
                        + " but be prepared!!!!")
                .build());
    return CommandResult.finish();
  }
}
