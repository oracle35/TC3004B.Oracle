package com.springboot.MyTodoList.bot.command.task;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.AuthenticatedTelegramCommand;
import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.TaskService;

public class DoneTaskCommand extends AuthenticatedTelegramCommand {

    private final TaskService taskService;
    private final Logger logger = LoggerFactory.getLogger(DoneTaskCommand.class);

    public DoneTaskCommand(TelegramClient client, TaskService taskService) {
        super(client);
        this.taskService = taskService;
    }

    @Override
    public String getDescription() {
        return "Shows the number of completed tasks per sprint for you.";
    }

    @Override
    public CommandResult executeAuthenticated(CommandContext context) {
        int userId = context.getAuthenticatedUser().getID_User();
        logger.info("Fetching tasks for user ID: {}", userId);

        // 1. Fetch all tasks assigned to the authenticated user
        List<Task> allUserTasks = taskService.findByAssignedTo(userId);

        // 2. Filter for completed tasks ("DONE" state)
        List<Task> completedTasks = allUserTasks.stream()
                .filter(task -> "DONE".equals(task.getState())) // Use equals for string comparison, safer with literals first
                .collect(Collectors.toList());
        logger.info("Found {} completed tasks for user ID: {}", completedTasks.size(), userId);


        // 3. Group completed tasks by Sprint ID and count them
        Map<Integer, Long> completedTasksBySprint = completedTasks.stream()
                .collect(Collectors.groupingBy(Task::getID_Sprint, Collectors.counting()));
        logger.info("Completed tasks grouped by sprint: {}", completedTasksBySprint);

        // 4. Format the message
        String messageText;
        if (completedTasksBySprint.isEmpty()) {
            // Use escaped period for MarkdownV2 safety even in simple messages
            messageText = "You haven't completed any tasks yet\\.";
        } else {
            StringBuilder sb = new StringBuilder();
            // Use MarkdownV2 bold for the header
            sb.append("*Completed Tasks by Sprint:*\n\n");

            // Sort by sprint ID for consistent output
            completedTasksBySprint.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        Integer sprintId = entry.getKey();
                        Long count = entry.getValue();
                        // Format each line and escape potential Markdown chars
                        String line = String.format("Sprint %d: %d completed task%s",
                                sprintId, count, count > 1 ? "s" : ""); // Handle plural "task(s)"
                        sb.append(escapeMarkdownV2(line)).append("\n");
                    });

            messageText = sb.toString();
        }

        // 5. Send the message
        sendMessage(context, msg -> msg.parseMode(ParseMode.MARKDOWNV2).text(messageText).build());

        return CommandResult.finish();
    }
}
