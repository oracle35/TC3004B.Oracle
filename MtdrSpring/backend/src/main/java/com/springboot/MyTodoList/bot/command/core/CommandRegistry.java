package com.springboot.MyTodoList.bot.command.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandRegistry {
  private final Map<String, TelegramCommand> commands = new HashMap<>();

  public void registerCommand(String name, TelegramCommand command) {
    command.setName(name);
    commands.put(name, command);
  }

  public Optional<TelegramCommand> findCommand(String commandName) {
    return Optional.ofNullable(commands.get(commandName));
  }

  public List<TelegramCommand> getAll() {
    return commands.values().stream().collect(Collectors.toList());
  }
}
