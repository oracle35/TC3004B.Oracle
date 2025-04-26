package com.springboot.MyTodoList.bot.command.core;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.name.BotName;

import com.springboot.MyTodoList.model.User;

public class CommandContext {
  private final Update update;
  private final BotName botName;
  private final Message message;
  private final CommandRegistry registry;
  private final Optional<User> user;
  private final Logger logger = LoggerFactory.getLogger(CommandContext.class);

  public CommandContext(Update update, CommandRegistry registry, BotName botName, Optional<User> user) {
    this.update = update;
    this.user = user; 
    this.botName = botName;
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

  /*
   * If the current command was canceled via /cancel.
   */
  public boolean isCancelled() {
    return (getArguments()[0].equals("/cancel"));
  }

  public boolean isAuthenticated() {
    return this.user.isPresent();
  }

  public Optional<User> getUser() {
    return this.user;
  }
  
  public String getBotUsername() {
    return this.botName.getName();
  }

  public User getAuthenticatedUser() {
    return this.user
      .orElseThrow(
          () -> 
            new IllegalStateException("authenticated but user is missing"));
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
