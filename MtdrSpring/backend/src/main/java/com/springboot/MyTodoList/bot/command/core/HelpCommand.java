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
  public CommandState execute(CommandContext context) {
    String messageText = context
      .getRegistry()
      .getAll()
      .stream()
      .map(
          cmd ->
            String.format("%s - %s", cmd.getName().substring(1), cmd.getDescription()))
      .collect(Collectors.joining("\n"));

    sendMessage(
        context,
        msg -> msg.text(messageText).build());
    return CommandState.FINISH;
  }
}

