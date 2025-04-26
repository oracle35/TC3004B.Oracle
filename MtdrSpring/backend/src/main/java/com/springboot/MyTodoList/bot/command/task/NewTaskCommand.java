package com.springboot.MyTodoList.bot.command.task;

import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.bot.command.core.AuthenticatedTelegramCommand;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.TaskService;

public class NewTaskCommand extends AuthenticatedTelegramCommand {
  private Map<Long, Task> partialItems = new HashMap<>();
  private final TaskService taskService;

  public NewTaskCommand(TelegramClient client, TaskService taskService) {
    super(client);
    this.taskService = taskService;
  }

  @Override
  public String getDescription() {
    return "Create a new task and assign it to yourself";
  }

  @Override
  public CommandResult executeAuthenticated(CommandContext context) {
    // Check if user ran /cancel
    if (context.isCancelled()) {
      sendMessage(context, "Operation cancelled.");
      partialItems.remove(context.getChatId());
      return CommandResult.finish();
    }

    Task item = partialItems.get(context.getChatId());
    if (item == null) {
      Task task = new Task();
      task.setAssignedTo(context.getAuthenticatedUser().getID_User());
      partialItems.put(context.getChatId(), task);
      sendMessage(context, "Give me a description for your new task!");
      return CommandResult.continu();
    }

    if (item.getDescription() == null) {
      item.setDescription(context.getMessage().getText());
      sendMessage(context, "Now, a delivery date in the format YYYY-MM-DD...");
    } else if (item.getFinishesAt() == null) {
      // Validate supplied date time
      try {
        String dateTimeString = context.getMessage().getText() + "T00:00:00+00:00";
        OffsetDateTime deliveryTime = OffsetDateTime.parse(dateTimeString);
        item.setFinishesAt(deliveryTime);
      } catch (DateTimeException e) {
        sendMessage(context, "Invalid date format. Please use YYYY-MM-DD! (e.g 2025-03-15)");
        return CommandResult.continu();
      }

      item.setCreatedAt(OffsetDateTime.now());
      item.setState("NOT_STARTED");
      sendMessage(context, "Finally, give me an estimation of how long you'll to complete this task in hours...");
    } else if (item.getHoursEstimated() == null) {
      // Validate hours given
      try {
        int estimate = Integer.parseInt(context.getMessage().getText());
        if (estimate < 1) {
          throw new NumberFormatException("Hours must be a positive number between 1 and 4.");
        } else if (estimate > 4) {
          throw new NumberFormatException("Due to internal policy no task may exceed 4 hours.");
        }
        item.setHoursEstimated(estimate);
      } catch (NumberFormatException e) {
        e.printStackTrace();
        sendMessage(context, "Invalid input: " + e.getLocalizedMessage());
        sendMessage(context, "Give me an estimation between 1 and 4 hours...");
        return CommandResult.continu();
      }
      
      taskService.addTask(item);
      sendMessage(context, "Item added!");
      partialItems.remove(context.getChatId());
      return CommandResult.finish();
    }

    return CommandResult.continu();
  }
}

