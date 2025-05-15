package com.springboot.MyTodoList.test.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.name.BotName;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.CommandContext;
import com.springboot.MyTodoList.bot.command.core.CommandRegistry;
import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.bot.command.task.NewTaskCommand;
import com.springboot.MyTodoList.service.TaskService;

class NewTaskTest {
    private NewTaskCommand command;
    private TelegramClient client;
    private TaskService taskService;
    private CommandRegistry registry;
    private Update update;
    private Message message;
    private User telegramUser;
    private BotName testBotName; 

    @BeforeEach
    void setUp() {
        client = mock(TelegramClient.class);
        taskService = mock(TaskService.class);
        registry = mock(CommandRegistry.class);
        update = mock(Update.class);
        message = mock(Message.class);
        telegramUser = mock(User.class);

        command = new NewTaskCommand(client, taskService);

        // Set up basic message behavior
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getFrom()).thenReturn(telegramUser);
        when(message.getChatId()).thenReturn(123L);
        when(message.getText()).thenReturn("/tasknew");
        testBotName = BotName.builder().name("TestBot").build(); 
    }

    @Test
    void testNewTaskInitialPrompt() throws TelegramApiException {
        // Setup
        when(message.getText()).thenReturn("/tasknew");
        CommandContext context = new CommandContext(
            new String[] { "/tasknew" }, 
            update, 
            registry, 
            testBotName, 
            Optional.of(new com.springboot.MyTodoList.model.User())
        );

        when(client.execute(any(SendMessage.class))).thenReturn(message);

        CommandResult result = command.executeAuthenticated(context);

        verify(client).execute(any(SendMessage.class));
        assert (result.getState() == CommandResult.CommandState.CONTINUE);
    }

    @Test
    void testNewTaskDescription() throws TelegramApiException {
        // Setup
        when(message.getText()).thenReturn("Test task description");
        CommandContext context = new CommandContext(
                new String[] { "Test task description" },
                update,
                registry,
                testBotName,
                Optional.of(new com.springboot.MyTodoList.model.User()));

        when(client.execute(any(SendMessage.class))).thenReturn(message);

        CommandResult result = command.executeAuthenticated(context);

        verify(client).execute(any(SendMessage.class));
        assert (result.getState() == CommandResult.CommandState.CONTINUE);
    }

}