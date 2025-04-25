package com.springboot.MyTodoList.bot.command.task;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.TelegramCommand;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.TaskService;

public class TaskListCommand extends TelegramCommand {
  private final TaskService taskService;
  private final Logger logger = LoggerFactory.getLogger(TaskListCommand.class);

  public TaskListCommand(TelegramClient client, TaskService toDoItemService) {
    super(client);
    this.taskService = toDoItemService;
  }

  @Override
  public String getDescription() {
    return "List all tasks assigned to you in the current sprint.";
  }

  @Override
  public CommandState execute(CommandContext context) {
    String[] args = context.getArguments();

    List<Task> allItems = taskService.findAll();
    logger.info("/list command found " + allItems.size() + " items");

    boolean listAllItems = args.length > 1 && args[1].equals("all");
    logger.info("listAllItems: " + listAllItems);

    List<Task> filteredItems =
        listAllItems
            ? allItems
            : taskService.findAll().stream()
                .filter(item -> item.getState().equals("IN_PROGRESS"))
                .collect(Collectors.toList());

    String messageText =
        filteredItems.size() > 0
            ? filteredItems.stream()
                .map(item -> String.format("%d - %s", item.getID_Task(), item.getDescription()))
                .collect(Collectors.joining("\n"))
            : "No items found. Good for you!";

    sendMessage(context, msg -> msg.text(messageText).build());
    return CommandState.FINISH;
  }
}
