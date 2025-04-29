package com.springboot.MyTodoList.bot.command.core;

import java.util.stream.Collectors;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public class HelpCommand extends TelegramCommand {
  public HelpCommand(TelegramClient client) {
    super(client);
  }

  @Override
  public String getDescription() {
    return "Show all commands along with a description.";
  }

  @Override
  public CommandResult execute(CommandContext context) {
    // TODO: change text depending on authentication
    // show only available commands depending on auth status
    String messageText = context
      .getRegistry()
      .getAll()
      .stream()
      .filter(cmd -> cmd.getName().startsWith("/"))
      .map(
          cmd ->
            String.format("%s - %s", cmd.getName().substring(1), cmd.getDescription()))
      .collect(Collectors.joining("\n"));

    sendMessage(
        context,
        msg -> msg.text(messageText).build());
    return CommandResult.finish();
  }
}

