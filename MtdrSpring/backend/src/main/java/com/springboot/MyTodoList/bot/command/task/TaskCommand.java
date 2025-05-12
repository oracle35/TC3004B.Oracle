package com.springboot.MyTodoList.bot.command.task;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.springboot.MyTodoList.bot.command.core.AuthenticatedTelegramCommand;
import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TaskService;

/**
 * The TaskCommand handles viewing, editing and managing
 * individual tasks. It's not visible as a user-facing command,
 * and doesn't start with a slash. This is on purpose, as its intended
 * use is to be invoked via deeplinks (see TaskListCommand) or
 * CallbackQuery's.
 */
public class TaskCommand extends AuthenticatedTelegramCommand {
  private Logger logger = LoggerFactory.getLogger(TaskCommand.class);
  private TaskService taskService;
  private SprintService sprintService;

  private final Pattern ID_MSG_PATTERN = Pattern.compile("id:(\\w+)");
  
  // Tasks marked as done need the amount taken to complete
  // this requires an additional interaction that needs to be tracked
  private final Map<Long, Integer> pendingFinishedTasks = new HashMap<>();

  /**
   * Represents each button to manage a task on the task
   * view message.
   */
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

    public static Optional<TaskAction> fromCallbackName(String query) {
      for (TaskAction action : values()) {
        if (query.equals(action.callbackName)) {
          return Optional.of(action);
        }
      }

      return Optional.empty();
    }

    public InlineKeyboardButton button() {
      return InlineKeyboardButton.builder().text(label).callbackData(callbackName).build();
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

  /**
   * Depending on the task state, different actions make sense to use.
   * For example, a finished task should not have a button to mark it as done.
   * This function determines the actions available given a certain task.
   */
  private InlineKeyboardMarkup keyboardForTask(Task task) {
    // IF java had pattern matching this wouldn't have to happen
    // i guess java 21+ has it but still
    InlineKeyboardRow taskRow =
        task.getState().equals("TODO")
            ? new InlineKeyboardRow(
                TaskAction.START.button(), TaskAction.DO.button(), TaskAction.BLOCKED.button())
            : task.getState().equals("IN_PROGRESS")
                ? new InlineKeyboardRow(TaskAction.DO.button(), TaskAction.BLOCKED.button())
                : task.getState().equals("BLOCKED")
                    ? new InlineKeyboardRow(TaskAction.START.button(), TaskAction.DO.button())
                    : new InlineKeyboardRow(TaskAction.UNDO.button());

    InlineKeyboardRow generalRow =
        new InlineKeyboardRow(TaskAction.EDIT.button(), TaskAction.DELETE.button());

    return InlineKeyboardMarkup.builder().keyboardRow(taskRow).keyboardRow(generalRow).build();
  }

  /**
   * Send a message with the task ID for use on callback queries.
   */
  private Optional<Message> sendIdMessage(Long chatId, int task) {
    try {
      Message result =
          this.client.execute(SendMessage.builder().chatId(chatId).text("📂 id:" + task).build());
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
    String sprint = task.getID_Sprint() > 0 ? String.valueOf(task.getID_Sprint()) : "No sprint";
    return String.format(
        taskTemplate,
        escapeMarkdownV2(task.getDescription()),
        escapeMarkdownV2(sprint),
        escapeMarkdownV2(task.getState()),
        escapeMarkdownV2(dueDate));
  }

  /**
   * When a user activates this command via a deeplink, the bot:
   * - Sends a message with the task ID
   * - Replies to it with the task contents, adding InlineKeyboardMarkup to
   *   allow the user to manage the task
   * The message with the ID allows the buttons to have static callback data,
   * simplifying some logic. Once the callback is received the bot can look up
   * the task the button refers to by the original message that was replied to.
   */
  private void viewTask(CommandContext context, Task task) {
    if (task.getAssignedTo() != context.getUser().get().getID_User()) {
      sendMessage(context, "This task is not assigned to you. You cannot view it.");
      return;
    }

    Message idMsg = sendIdMessage(context.getChatId(), task.getID_Task()).get();

    sendMessage(
        context,
        msg ->
            msg.parseMode(ParseMode.MARKDOWNV2)
                .text(getTaskText(task))
                .replyMarkup(keyboardForTask(task))
                .replyToMessageId(idMsg.getMessageId())
                .build());
  }

  /**
   * Given a task action, edit the task object, update it on the database,
   * and edit the message on the button to reflect the changes.
   */
  private CommandResult changeTaskState(
      CallbackQuery query, int taskId, Message taskMessage, TaskAction action) {
    Task task = taskService.findById(taskId).get();
    switch (action) {
      case DO:
        task.setState("DONE");
        sendMessage(taskMessage.getChatId(), "How many hours did it take?"); 
        pendingFinishedTasks.put(taskMessage.getChatId(), taskId);
        return CommandResult.continu();
      case BLOCKED:
        task.setState("BLOCKED");
        break;
      case START:
        task.setState("IN_PROGRESS");
        break;
      case UNDO:
        task.setState("IN_PROGRESS");
        break;
      default:
    }
    taskService.updateTask(taskId, task);
    
    EditMessageText editMsg =
        EditMessageText.builder()
            .parseMode(ParseMode.MARKDOWNV2)
            .chatId(taskMessage.getChatId())
            .messageId(taskMessage.getMessageId())
            .text(getTaskText(task))
            .replyMarkup(keyboardForTask(task))
            .build();

    try {
      client.execute(editMsg);
      answerCallbackQuery(query, "Task updated!");
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
    return CommandResult.finish();
  }

  /**
   * Handles callback queries for task actions.
   * @param context The command context containing the callback query
   */
  private CommandResult handleCallbackQuery(CommandContext context) {
    CallbackQuery query = context.getCallbackQuery().get();
    
    // Telegram disallows viewing a message's contents once it's too old.
    var maybeMessage = query.getMessage();
    if (maybeMessage.getDate() == 0) {
      answerCallbackQuery(query, "Sorry, the message is too old. Try viewing the task again.");
      return CommandResult.finish();
    }

    Message message = (Message) query.getMessage();
    TaskAction action = TaskAction.fromCallbackName(query.getData()).get();
    if (action.getType() == "action") {
      // TODO: implement editing and deleting tasks
      answerCallbackQuery(query, "Sorry, I can't do that yet.");
      return CommandResult.finish();
    }

    String messageIdText = message.getReplyToMessage().getText();
    Matcher idMatcher = ID_MSG_PATTERN.matcher(messageIdText);
    if (!idMatcher.find()) return CommandResult.finish();

    int taskId = Integer.parseInt(idMatcher.group(1));
    return changeTaskState(query, taskId, message, action);
  }

  /*
   * Tasks marked as done need additional logic, as the developer
   * must provide the hours taken to complete it. This is used
   * for statistical purposes and for calculation of key performance
   * indicators.
   */
  private CommandResult handleFinishedTask(CommandContext context, int taskId) {
    if (!context.getMessage().isPresent()) {
      sendMessage(context, "Something went wrong. Please try again.");
      return CommandResult.finish();
    }
    
    try {
      int hours = Integer.parseInt(context.getMessage().get().getText());
      if (hours < 1) {
        sendMessage(context, "You must input a number greater than 0.");
        return CommandResult.continu();
      }
      Task task = taskService.findById(taskId).get();
      task.setHoursReal(hours);
      task.setState("DONE");
      taskService.updateTask(taskId, task);
      pendingFinishedTasks.remove(context.getChatId());
      sendMessage(context, "Done! Task updated.");
    } catch (NumberFormatException e) {
      sendMessage(context, "You must input a number!");
      return CommandResult.continu();
    }
    return CommandResult.finish();
  }

  public CommandResult executeAuthenticated(CommandContext context) {
    // First, handle callback queries
    if (context.hasCallbackQuery()) {
      return handleCallbackQuery(context);
    }
    
    // For regular message updates
    if (pendingFinishedTasks.containsKey(context.getChatId())) {
      return handleFinishedTask(context, pendingFinishedTasks.get(context.getChatId()));
    }

    if (!context.hasArguments()) {
      sendMessage(context, "You must give a task ID to view a task!");
      return CommandResult.finish();
    }

    try {
      int taskId = Integer.parseInt(context.getArguments()[1]);
      taskService
          .findById(taskId)
          .ifPresentOrElse(
              task -> viewTask(context, task), () -> sendMessage(context, "Task not found!"));
    } catch (NumberFormatException e) {
      sendMessage(context, "Invalid parameter: you must supply a number.");
    }

    return CommandResult.finish();
  }
}
