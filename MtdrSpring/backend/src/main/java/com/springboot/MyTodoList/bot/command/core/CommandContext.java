package com.springboot.MyTodoList.bot.command.core;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public class CommandContext {
  private final Update update;
  private final Message message;
  private final CommandRegistry registry;

  public CommandContext(Update update, CommandRegistry registry) {
    this.update = update;
    this.registry = registry;
    this.message = update.getMessage();
  }

  public Message getMessage() {
    return this.message;
  }
  
  /*
   * Get the command registry.
   * Be careful when using it as it may lead to recursion.
   */
  public CommandRegistry getRegistry() {
    return this.registry;
  }

  public String[] getArguments() {
    // TODO: this assumes the message has text. Will it always?
    return this.message.getText().split("\\s+");
  }

  public Long getChatId() {
    return this.message.getChatId();
  }

  public User getSender() {
    return this.message.getFrom();
  }

  public Long getSenderId() {
    return this.message.getFrom().getId();
  }
}
