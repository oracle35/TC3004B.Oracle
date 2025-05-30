package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.TaskDependency;
import com.springboot.MyTodoList.service.TaskDependencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taskDependency")
@Tag(name = "Dependencias de Tareas", description = "Operaciones CRUD para la entidad Dependencias de Tareas")
public class TaskDependencyController {

    @Autowired
    private TaskDependencyService taskDependencyService;

    // Obtener todas las dependencias de tareas
    @GetMapping
    @Operation(
            summary = "Obtener todas las dependencias de tareas",
            description = "Devuelve una lista de todas las dependencias de tareas, sin filtros."
    )
    public ResponseEntity<List<TaskDependency>> getAllTaskDependencies() {
        List<TaskDependency> dependencies = taskDependencyService.findAll();
        return ResponseEntity.ok(dependencies);
    }

    // Obtener una dependencia de tarea por ID
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una dependencia de tarea por ID",
            description = "Devuelve una dependencia de tarea específica según el ID proporcionado."
    )
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
    @Operation(
            summary = "Crear una nueva dependencia de tarea",
            description = "Crea una nueva dependencia de tarea y devuelve la entidad creada."
    )
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
    @Operation(
            summary = "Actualizar una dependencia de tarea existente",
            description = "Actualiza los datos de una dependencia de tarea específica basada en su ID."
    )
    public ResponseEntity<TaskDependency> updateTaskDependency(
            @RequestBody TaskDependency updatedDependency, @PathVariable int id) {
        try {
            TaskDependency updated = taskDependencyService.updateTaskDependency(id, updatedDependency);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Eliminar una dependencia de tarea
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar una dependencia de tarea",
            description = "Elimina una dependencia de tarea específica basada en su ID."
    )
    public ResponseEntity<Void> deleteTaskDependency(@PathVariable int id) {
        try {
            taskDependencyService.deleteTaskDependency(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
