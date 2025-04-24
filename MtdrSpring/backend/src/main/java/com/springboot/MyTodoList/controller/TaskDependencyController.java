package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.TaskDependency;
import com.springboot.MyTodoList.service.TaskDependencyService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/taskDependency")
public class TaskDependencyController {

    @Autowired private TaskDependencyService taskDependencyService;

    // Obtener todas las dependencias de tareas
    @GetMapping
    public ResponseEntity<List<TaskDependency>> getAllTaskDependencies() {
        List<TaskDependency> dependencies = taskDependencyService.findAll();
        return ResponseEntity.ok(dependencies);
    }

    // Obtener una dependencia de tarea por ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskDependency> getTaskDependencyById(@PathVariable int id) {
        try {
            ResponseEntity<TaskDependency> response = taskDependencyService.getItemById(id);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Crear una nueva dependencia de tarea
    @PostMapping
    public ResponseEntity<TaskDependency> addNewTaskDependency(
            @RequestBody TaskDependency newDependency) throws Exception {
        TaskDependency createdDependency = taskDependencyService.addTaskDependency(newDependency);

        HttpHeaders headers = new HttpHeaders();
        headers.set("location", String.valueOf(createdDependency.getID_Task_Parent()));
        headers.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(createdDependency);
    }

    // Actualizar una dependencia de tarea
    @PutMapping("/{id}")
    public ResponseEntity<TaskDependency> updateTaskDependency(
            @RequestBody TaskDependency updatedDependency, @PathVariable int id) {
        try {
            TaskDependency updated =
                    taskDependencyService.updateTaskDependency(id, updatedDependency);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Eliminar una dependencia de tarea
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskDependency(@PathVariable int id) {
        try {
            taskDependencyService.deleteTaskDependency(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
