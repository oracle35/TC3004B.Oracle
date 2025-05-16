package com.springboot.MyTodoList.test.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.springboot.MyTodoList.bot.command.core.CommandResult;
import com.springboot.MyTodoList.bot.command.task.NewTaskCommand;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.SprintService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.test.bot.util.CommandTester;
import com.springboot.MyTodoList.test.bot.util.ConversationTester;

import static org.mockito.Mockito.*;

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

    tester.sendMessage("2")
        .assertResult(CommandResult.finish())
        .assertContains("Item added!");

    verify(taskService).addTask(
        argThat(
          task -> 
            task.getDescription().equals("Test task description") &&
            task.getAssignedTo() == 1 &&
            task.getHoursEstimated() == 2 &&
            task.getState().equals("TODO")));
  }
}
