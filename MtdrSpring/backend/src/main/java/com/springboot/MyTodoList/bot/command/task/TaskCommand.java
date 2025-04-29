package com.springboot.MyTodoList.bot.command.task;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.webjars.NotFoundException;

import com.springboot.MyTodoList.bot.command.core.AuthenticatedTelegramCommand;
import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TaskService;

public class TaskCommand extends AuthenticatedTelegramCommand {
  private TaskService taskService;
  private SprintService sprintService;

  private final Pattern ID_MSG_PATTERN = Pattern.compile("id:(\\w+)");

  private enum TaskAction {
    DO("Done", "task_done", "state"),
    START("Start", "task_start", "state"),
    UNDO("Not finished", "task_undone", "state"),
    BLOCKED("Blocked", "task_blocked", "state"),
    DELETE("Delete", "task_delete", "action"),
    EDIT("Edit", "task_edit", "action");

    private final String label;
    private final String callbackName;
    private final String type;

    TaskAction(String label, String callbackName, String type) {
      this.label = label;
      this.callbackName = callbackName;
      this.type = type;
    }

    public String getLabel() {
      return this.label;
    }

    public String getType() {
      return this.type;
    }

    public static TaskAction fromCallbackName(String query) {
      for (TaskAction action : values()) {
        if (query.equals(action.callbackName)) {
          return action;
        }
      }

      throw new NotFoundException(query + " does not have a TaskAction.");
    }

    public InlineKeyboardButton button() {
      return InlineKeyboardButton
        .builder()
        .text(label)
        .callbackData(callbackName)
        .build();
    }
  }

  public TaskCommand(TelegramClient client, TaskService taskService, SprintService sprintService) {
    super(client);
    this.taskService = taskService;
    this.sprintService = sprintService;
  }

  @Override
  public String getDescription() {
    return "View the details of a task.";
  }

  private InlineKeyboardMarkup keyboardForTask(Task task) {
    int id = task.getID_Task(); 

    // task state isn't an enum
    // so I had to do this horrible thing
    InlineKeyboardRow taskRow =
      task.getState().equals("NOT_STARTED")
      ? new InlineKeyboardRow(TaskAction.START.button(), TaskAction.DO.button(), TaskAction.BLOCKED.button())
      : task.getState().equals("TODO")
      ? new InlineKeyboardRow(TaskAction.DO.button(), TaskAction.BLOCKED.button())
      : task.getState().equals("BLOCKED")
      ? new InlineKeyboardRow(TaskAction.START.button(), TaskAction.DO.button())
      : new InlineKeyboardRow(TaskAction.UNDO.button());
     
    InlineKeyboardRow generalRow = new InlineKeyboardRow(
        TaskAction.EDIT.button(),
        TaskAction.DELETE.button());
    
    return InlineKeyboardMarkup
      .builder()
      .keyboardRow(taskRow)
      .keyboardRow(generalRow)
      .build();
  }

  private Optional<Message> sendIdMessage(Long chatId, int task) {
    try {
      Message result = this.client.execute(SendMessage
          .builder()
          .chatId(chatId)
          .text("📂 id:" + task)
          .build());
      return Optional.of(result);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  private String getTaskText(Task task) {

    String taskTemplate = "*%s*\nSprint: %s\nState: *%s*\nDue: %s";
    String dueDate = 
      task.getFinishesAt() != null
      ? task.getFinishesAt().format(DateTimeFormatter.ISO_LOCAL_DATE)
      : "No due date";
    String sprint =
      task.getID_Sprint() > 0
      ? String.valueOf(task.getID_Sprint())
      : "No sprint";
    return String.format(
        taskTemplate,
        escapeMarkdownV2(task.getDescription()),
        escapeMarkdownV2(sprint),
        escapeMarkdownV2(task.getState()),
        escapeMarkdownV2(dueDate));
  }

  private void viewTask(CommandContext context, Task task) {
    if (task.getAssignedTo() != context.getUser().get().getID_User()) {
      sendMessage(context, "This task is not assigned to you. You cannot view it.");
    }

    Message idMsg = sendIdMessage(context.getChatId(), task.getID_Task()).get();

    sendMessage(
        context,
        msg -> msg
          .parseMode(ParseMode.MARKDOWNV2)
          .text(getTaskText(task))
          .replyMarkup(keyboardForTask(task))
          .replyToMessageId(idMsg.getMessageId())
          .build());
  }

  private void changeTaskState(int taskId, Message taskMessage, TaskAction action) {
    Task task = taskService.findById(taskId).get();
    switch (action) {
      case DO:
        task.setState("DONE");
        break;
      case BLOCKED:
        task.setState("BLOCKED");
        break;
      case START:
        task.setState("IN_PROGRESS");
        break;
      case UNDO:
        task.setState("TODO");
        break;
      default:
    }
    taskService.updateTask(taskId, task);
    EditMessageText editMsg = EditMessageText
      .builder()
      .parseMode(ParseMode.MARKDOWNV2)
      .chatId(taskMessage.getChatId())
      .messageId(taskMessage.getMessageId())
      .text(getTaskText(task))
      .replyMarkup(keyboardForTask(task))
      .build();

    try {
      client.execute(editMsg);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void callbackQuery(CallbackQuery query) {
    var maybeMessage = query.getMessage();
    if (maybeMessage.getDate() == 0) {
      answerCallbackQuery(query, "Sorry, the message is too old. Try viewing the task again.");
    }

    Message message = (Message)query.getMessage();
    TaskAction action = TaskAction.fromCallbackName(query.getData());
    if (action.getType() == "action") {
      answerCallbackQuery(query, "Sorry, I can't do that yet.");
    } else {
      String messageIdText = message.getReplyToMessage().getText();
      Matcher idMatcher = ID_MSG_PATTERN.matcher(messageIdText);
      if (!idMatcher.find()) return;
      
      int taskId = Integer.parseInt(idMatcher.group(1));
      changeTaskState(taskId, message, action);
    }
  }

  public CommandResult executeAuthenticated(CommandContext context) {
    if (!context.hasArguments()) {
      sendMessage(context, "You must give a task ID to view a task!");
      return CommandResult.finish();
    }
    
    try {
      int taskId = Integer.parseInt(context.getArguments()[1]);
      taskService.findById(taskId).ifPresentOrElse(
          task -> viewTask(context, task),
          () -> sendMessage(context, "Task not found!"));
    } catch (NumberFormatException e) {
      sendMessage(context, "Invalid parameter: you must supply a number.");
    }

    return CommandResult.finish();
  }
}

