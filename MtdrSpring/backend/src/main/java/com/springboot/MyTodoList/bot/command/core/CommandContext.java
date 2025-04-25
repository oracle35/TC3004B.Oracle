package com.springboot.MyTodoList.bot.command.core;

import java.util.Optional;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import com.springboot.MyTodoList.model.User;

public class CommandContext {
  private final Update update;
  private final Message message;
  private final CommandRegistry registry;
  private final Optional<User> user;

  public CommandContext(Update update, CommandRegistry registry, Optional<User> user) {
    this.update = update;
    this.user = user;
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

  public boolean isAuthenticated() {
    return this.user.isPresent();
  }

  public Optional<User> getUser() {
    return this.user;
  }

  public Long getChatId() {
    return this.message.getChatId();
  }

  public org.telegram.telegrambots.meta.api.objects.User getSender() {
    return this.message.getFrom();
  }

  public Long getSenderId() {
    return this.message.getFrom().getId();
  }
}
