package com.springboot.MyTodoList.bot.command.core;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();
    private final List<Command> contextualCommands = new ArrayList<>();

    public void registerCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public Optional<Command> findCommand(String commandName) {
        return Optional.ofNullable(commands.get(commandName));
    }
}


