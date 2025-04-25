package com.springboot.MyTodoList.bot.command.task;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.AuthenticatedTelegramCommand;
import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TaskService;

public class TaskListCommand extends AuthenticatedTelegramCommand {
  private final TaskService taskService;
  private final SprintService sprintService;
  private final Logger logger = LoggerFactory.getLogger(TaskListCommand.class);

  public TaskListCommand(TelegramClient client, TaskService toDoItemService, SprintService sprintService) {
    super(client);
    this.sprintService = sprintService;
    this.taskService = toDoItemService;
  }

  @Override
  public String getDescription() {
    return "List all tasks assigned to you in the current sprint.";
  }

  public List<Task> getTasks(CommandContext context, boolean showAll) {
    List<Task> allItems = taskService.findByAssignedTo(context.getSenderId());
    return showAll 
        ? allItems
        : allItems.stream()
            .filter(item -> !item.getState().equals("DONE"))
            .collect(Collectors.toList());
  };

  @Override
  public CommandState executeAuthenticated(CommandContext context) {
    String[] args = context.getArguments();


    boolean listAllItems = args.length > 1 && args[1].equals("all");
    List<Task> tasks = getTasks(context, listAllItems);

    String messageText =
        tasks.size() > 0
            ? tasks.stream()
                .map(item -> String.format("%d - %s", item.getID_Task(), item.getDescription()))
                .collect(Collectors.joining("\n"))
            : "No items found. Good for you!";

    sendMessage(context, msg -> msg.text(messageText).build());
    return CommandState.FINISH;
  }
}
