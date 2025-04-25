package com.springboot.MyTodoList.bot.command.task;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.TelegramCommand;
import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.service.ToDoItemService;

public class TaskListCommand extends TelegramCommand {
    private final ToDoItemService toDoItemService;
    private final Logger logger = LoggerFactory.getLogger(TaskListCommand.class);
    
    public TaskListCommand(TelegramClient client, ToDoItemService toDoItemService) {
        super(client);
        this.toDoItemService = toDoItemService;
    }

    @Override
    public String getDescription() {
        return "List all tasks assigned to you in the current sprint.";
    }

    @Override
    public CommandState execute(CommandContext context, TelegramClient client) {
        List<ToDoItem> allItems = toDoItemService.findAll();
        logger.info("/list command found " + allItems.size() + " items");

        boolean listAllItems = (context.getArguments()[1]).equals("all");
        logger.info("listAllItems: " + listAllItems);

        List<ToDoItem> filteredItems = listAllItems ? allItems : toDoItemService.findAll().stream()
            .filter(item -> item.getState().equals("IN_PROGRESS"))
            .collect(Collectors.toList());

        String messageText = filteredItems.size() > 0 ? filteredItems.stream()
            .map(item -> String.format("%d - %s", item.getID_Task(), item.getDescription()))
            .collect(Collectors.joining("\n")) :  "No items found. Good for you!";

        sendMessage(
            context,
            msg -> msg.text(messageText).build()
        );
        return CommandState.FINISH;
    }
}

