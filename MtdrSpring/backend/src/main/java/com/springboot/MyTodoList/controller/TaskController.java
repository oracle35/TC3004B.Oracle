package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Obtener todas las tareas
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.findAll();
        return ResponseEntity.ok(tasks);
    }

    // Obtener una tarea por ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable int id) {
        try {
            ResponseEntity<Task> response = taskService.getItemById(id);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Crear una nueva tarea
    @PostMapping
    public ResponseEntity<Task> addNewTask(@RequestBody Task newTask) throws Exception {
        Task createdTask = taskService.addTask(newTask);

        HttpHeaders headers = new HttpHeaders();
        headers.set("location", String.valueOf(createdTask.getID_Task()));
        headers.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .body(createdTask);
    }

    // Actualizar una tarea existente
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@RequestBody Task updatedTask, @PathVariable int id) {
        try {
            Task task = taskService.updateTask(id, updatedTask);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Eliminar una tarea
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable int id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
