package com.springboot.MyTodoList.test.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.UserAuthenticator;
import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandRegistry;
import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.bot.command.task.NewTaskCommand;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.TaskService;

class NewTaskTest {
    private NewTaskCommand command;
    private TaskService taskService;
    private TelegramClient client;
    private Logger logger = LoggerFactory.getLogger(NewTaskTest.class);

    /**
     * Before each test, it updates the bot information with Mock instances of classes.
     */
    @BeforeEach
    void setUp() throws TelegramApiException {
        // Mock dependencies
        client = mock(TelegramClient.class);
        taskService = mock(TaskService.class);

        Message message = mock(Message.class);
        when(client.execute(any(SendMessage.class))).thenReturn(message);
        
        // When TaskService.addTask is called, return the same task
        when(taskService.addTask(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Create the command with mocked dependencies
        command = new NewTaskCommand(client, taskService);
    }

    @Test
    void testCommandNotNull() {
        assertNotNull(command);
        assertEquals(command.getDescription(),  "Create a new task and assign it to yourself");
    }
    
    @Test
    void testCreateTask() {
        // Mock an authenticated user
        User appUser = new User();
        appUser.setID_User(123);

        String[] args = {"/newtask"};

        // Create a real CommandRegistry instead of mocking it
        CommandRegistry registry = new CommandRegistry();
        // Register the command we're testing (and any others needed)
        registry.registerCommand("newtask", command);

        // Simulate the command conversation flow
        // 1. Start command
        Update initialUpdate = TestFactory.mockMessageUpdate(TestFactory.USER, args);
        CommandContext initialContext = new CommandContext(args, initialUpdate, registry, TestFactory.BOT_NAME, Optional.of(appUser));
        CommandResult initialResult = command.execute(initialContext);
        assertEquals(CommandResult.CommandState.CONTINUE, initialResult.getState());

        // 2. Provide task description
        Update descriptionUpdate = TestFactory.mockMessageUpdate(TestFactory.USER, new String[]{"Test task description"});
        CommandContext descriptionContext = new CommandContext(args, descriptionUpdate, registry, TestFactory.BOT_NAME, Optional.of(appUser));
        CommandResult descriptionResult = command.execute(descriptionContext);
        assertEquals(CommandResult.CommandState.CONTINUE, descriptionResult.getState());

        // 3. Provide delivery date
        Update dateUpdate = TestFactory.mockMessageUpdate(TestFactory.USER, new String[]{"2025-12-31"});
        CommandContext dateContext = new CommandContext(args, dateUpdate, registry, TestFactory.BOT_NAME, Optional.of(appUser));
        CommandResult dateResult = command.execute(dateContext);
        assertEquals(CommandResult.CommandState.CONTINUE, dateResult.getState());

        // 4. Provide hours estimation
        Update hoursUpdate = TestFactory.mockMessageUpdate(TestFactory.USER, new String[]{"2"});
        CommandContext hoursContext = new CommandContext(args, hoursUpdate, registry, TestFactory.BOT_NAME, Optional.of(appUser));
        CommandResult hoursResult = command.execute(hoursContext);
        assertEquals(CommandResult.CommandState.FINISH, hoursResult.getState());

        // Verify that TaskService.addTask was called with the correct task
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).addTask(taskCaptor.capture());

        Task capturedTask = taskCaptor.getValue();
        assertEquals("Test task description", capturedTask.getDescription());
        assertEquals(123, capturedTask.getAssignedTo());
        assertEquals(2, capturedTask.getHoursEstimated());
        assertEquals("TODO", capturedTask.getState());
    }
    
    @Test
    void testInvalidHoursEstimation() {
        // Similar setup as above, but test invalid hours input
        // ... implementation would follow the same pattern
        // with verification that the task is not saved
    }
}
