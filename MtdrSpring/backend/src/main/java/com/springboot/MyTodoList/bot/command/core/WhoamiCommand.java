package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class WhoamiCommand extends TelegramCommand {
  public WhoamiCommand(TelegramClient client) {
    super(client);
  }

  @Override
  public String getDescription() {
    return "Show your telegram ID and other info.";
  }

  @Override
  public CommandResult execute(CommandContext context) {
    User user = context.getSender();
    String messageText =
        String.format(
            "Telegram ID \\- `%s`\n" + "Username \\- @%s", user.getId(), user.getUserName());

    sendMessage(context, msg -> msg.parseMode(ParseMode.MARKDOWNV2).text(messageText).build());
    return CommandResult.finish();
  }
}
