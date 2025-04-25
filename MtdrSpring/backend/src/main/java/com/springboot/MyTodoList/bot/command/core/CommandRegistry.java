package com.springboot.MyTodoList.bot.command.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandRegistry {
    private final Map<String, TelegramCommand> commands = new HashMap<>();

    public void registerCommand(String name, TelegramCommand command) {
        commands.put(name, command);
    }

    public Optional<TelegramCommand> findCommand(String commandName) {
        return Optional.ofNullable(commands.get(commandName));
    }
}
