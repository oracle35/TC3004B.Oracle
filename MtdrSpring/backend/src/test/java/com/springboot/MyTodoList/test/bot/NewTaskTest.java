package com.springboot.MyTodoList.test.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.UserAuthenticator;
import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.bot.command.task.NewTaskCommand;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.TaskService;

class NewTaskTest {
    private NewTaskCommand command;
    private TaskService taskService;
    private TelegramClient client;
    private UserAuthenticator authenticator;

    /**
     * Before each test, it updates the bot information with Mock instances of classes.
     */
    @BeforeEach
    void setUp() {
        // Mock dependencies
        client = mock(TelegramClient.class);
        taskService = mock(TaskService.class);
        authenticator = mock(UserAuthenticator.class);
        
        // When TaskService.addTask is called, return the same task
        when(taskService.addTask(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Create the command with mocked dependencies
        command = new NewTaskCommand(client, taskService);
    }
    
    @Test
    void testCreateTask() {
        // Mock an authenticated user
        User appUser = new User();
        appUser.setID_User(123);
        
        // Set up the authenticator to return our mocked user
        when(authenticator.authenticateUser(TestFactory.USER)).thenReturn(Optional.of(appUser));
        
        // Simulate the command conversation flow
        // 1. Start command
        Update initialUpdate = TestFactory.mockMessageUpdate(TestFactory.USER, new String[]{"/newtask"});
        CommandContext initialContext = new CommandContext(initialUpdate, authenticator);
        CommandResult initialResult = command.executeAuthenticated(initialContext);
        assertEquals(CommandResult.CONTINUE, initialResult);
        
        // 2. Provide task description
        Update descriptionUpdate = TestFactory.mockMessageUpdate(TestFactory.USER, new String[]{"Test task description"});
        CommandContext descriptionContext = new CommandContext(descriptionUpdate, authenticator);
        CommandResult descriptionResult = command.executeAuthenticated(descriptionContext);
        assertEquals(CommandResult.CONTINUE, descriptionResult);
        
        // 3. Provide delivery date
        Update dateUpdate = TestFactory.mockMessageUpdate(TestFactory.USER, new String[]{"2025-12-31"});
        CommandContext dateContext = new CommandContext(dateUpdate, authenticator);
        CommandResult dateResult = command.executeAuthenticated(dateContext);
        assertEquals(CommandResult.CONTINUE, dateResult);
        
        // 4. Provide hours estimation
        Update hoursUpdate = TestFactory.mockMessageUpdate(TestFactory.USER, new String[]{"2"});
        CommandContext hoursContext = new CommandContext(hoursUpdate, authenticator);
        CommandResult hoursResult = command.executeAuthenticated(hoursContext);
        assertEquals(CommandResult.FINISH, hoursResult);
        
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
