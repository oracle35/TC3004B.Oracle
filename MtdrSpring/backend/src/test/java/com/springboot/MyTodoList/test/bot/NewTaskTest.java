package com.springboot.MyTodoList.test.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.bot.command.task.NewTaskCommand;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.test.bot.util.CommandTester;

@ExtendWith(MockitoExtension.class)
public class NewTaskTest {
    @Mock
    private TelegramClient client;

    @Mock
    private TaskService taskService;

    @Mock
    private SprintService sprintService;

    private CommandTester tester;

    @BeforeEach
    public void setUp() {
        tester = CommandTester.create()
                .withCommand("/tasknew", mockClient -> new NewTaskCommand(mockClient, taskService))
                .withAuthentication();
    }

    @Test
    public void testCreateTask() {
        tester.sendMessage("/newtask")
                .assertResult(CommandResult.continu())
                .assertContains("description");

        tester.sendMessage("Test task description")
                .assertResult(CommandResult.continu())
                .assertContains("delivery date");

        tester.sendMessage("2025-12-31")
                .assertResult(CommandResult.continu())
                .assertContains("estimation");
                
        // Hours Estimated
        tester.sendMessage("2")
                .assertResult(CommandResult.continu())
                .assertContains("Story Points");

        // Story Points
        tester.sendMessage("3") // <-- Change to "3" to match the verify below
                .assertResult(CommandResult.finish())
                .assertContains("Item added"); // <-- Espera el prompt de story points
        verify(taskService).addTask(
                argThat(
                        task -> task.getDescription().equals("Test task description") &&
                                task.getAssignedTo() == 1 &&
                                task.getHoursEstimated() == 2 &&
                                task.getStoryPoints() == 3 &&
                                task.getState().equals("TODO")));
    }

    @Test
    public void testCreateTask_InvalidDateFormat() {
        tester.sendMessage("/newtask")
                .assertResult(CommandResult.continu())
                .assertContains("description");

        tester.sendMessage("Test task description")
                .assertResult(CommandResult.continu())
                .assertContains("delivery date");

        tester.sendMessage("invalid-date")
                .assertResult(CommandResult.continu())
                .assertContains("Invalid");

        tester.sendMessage("2")
                .assertResult(CommandResult.continu());

        verify(taskService, never()).addTask(any());
    }

    @Test
    public void testCreateTask_InvalidHours() {
        tester.sendMessage("/newtask")
                .assertResult(CommandResult.continu())
                .assertContains("description");

        tester.sendMessage("Test task description")
                .assertResult(CommandResult.continu())
                .assertContains("delivery date");

        tester.sendMessage("2025-12-31")
                .assertResult(CommandResult.continu())
                .assertContains("estimation");

        tester.sendMessage("0")
                .assertResult(CommandResult.continu());

        tester.sendMessage("5")
                .assertResult(CommandResult.continu());
        verify(taskService, never()).addTask(any());
    }

    @Test
    public void testCreateTask_Cancel() {
        tester.sendMessage("/newtask")
                .assertResult(CommandResult.continu());

        tester.sendMessage("/cancel")
                .assertContains("cancel")
                .assertResult(CommandResult.finish());

        verify(taskService, never()).addTask(any());
    }
}
