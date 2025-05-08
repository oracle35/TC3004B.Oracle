package com.springboot.MyTodoList.bot.command.core;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.name.BotName;

import com.springboot.MyTodoList.model.User;

public class CommandContext {
  private final Update update;
  private final BotName botName;
  private final CommandRegistry registry;
  private final String[] args;
  private final Optional<User> user;
  private final Logger logger = LoggerFactory.getLogger(CommandContext.class);

  public CommandContext(
      String[] args,
      Update update,
      CommandRegistry registry,
      BotName botName,
      Optional<User> user) {
    this.update = update;
    this.user = user;
    this.args = args;
    this.botName = botName;
    this.registry = registry;
  }

  /**
   * Get the original update that created this context.
   */
  public Update getUpdate() {
    return this.update;
  }

  /**
   * Get the message object if present in the update.
   * For callback queries, this will return the message the callback was attached to.
   */
  public Optional<Message> getMessage() {
    if (update.hasMessage()) {
      return Optional.of(update.getMessage());
    } else if (update.hasCallbackQuery()) {
      return Optional.of((Message) update.getCallbackQuery().getMessage());
    }
    return Optional.empty();
  }

  /**
   * Get the callback query if present in the update.
   */
  public Optional<CallbackQuery> getCallbackQuery() {
    if (update.hasCallbackQuery()) {
      return Optional.of(update.getCallbackQuery());
    }
    return Optional.empty();
  }

  /**
   * Check if this context contains a message.
   */
  public boolean hasMessage() {
    return update.hasMessage();
  }

  /**
   * Check if this context contains a callback query.
   */
  public boolean hasCallbackQuery() {
    return update.hasCallbackQuery();
  }

  /*
   * Get the command registry.
   * Be careful when using it as it may lead to recursion.
   */
  public CommandRegistry getRegistry() {
    return this.registry;
  }

  public String[] getArguments() {
    return args;
  }

  public boolean hasArguments() {
    return getArguments().length > 1;
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
    return this.user.orElseThrow(
        () -> new IllegalStateException("authenticated but user is missing"));
  }

  public Long getChatId() {
    if (update.hasMessage()) {
      return update.getMessage().getChatId();
    } else if (update.hasCallbackQuery()) {
      return update.getCallbackQuery().getMessage().getChatId();
    }
    throw new IllegalStateException("No chat ID available in this update");
  }

  public org.telegram.telegrambots.meta.api.objects.User getSender() {
    if (update.hasMessage()) {
      return update.getMessage().getFrom();
    } else if (update.hasCallbackQuery()) {
      return update.getCallbackQuery().getFrom();
    }
    throw new IllegalStateException("No sender available in this update");
  }

  public Long getSenderId() {
    return getSender().getId();
  }
}
