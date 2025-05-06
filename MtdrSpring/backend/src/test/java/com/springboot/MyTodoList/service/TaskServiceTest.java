package com.springboot.MyTodoList.service;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepositoryMock; // Mocking the dependency

    @InjectMocks
    private TaskService taskService; // We inject the service of the dependency we are testing

    @Test
    public void testGetTaskById_TaskExists() {
        Task expectedTask = new Task(1, "Test Task", "TODO", 5, 0, 1, 1, OffsetDateTime.now(), null, null);
        when(taskRepositoryMock.findById(1)).thenReturn(Optional.of(expectedTask)); // 3. Stubbing

        ResponseEntity<Task> actualTaskOptional = taskService.getItemById(1);

        assertNotNull(actualTaskOptional);
        assertEquals(200, actualTaskOptional.getStatusCodeValue());
        verify(taskRepositoryMock, times(1)).findById(1); // Verificar interacción
    }

}