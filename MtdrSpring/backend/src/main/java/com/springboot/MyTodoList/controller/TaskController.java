package com.springboot.MyTodoList.controller;

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

import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * TODO: Implementar Query Params para la busqueda de tareas.
 * Esto se utilizaría en la busqueda de tareas con una barra de busqueda
 * (funcionalidad 100% opcional)
 */
@RestController
@RequestMapping("/task")
@Tag(name = "Tareas", description = "Operaciones CRUD para la entidad Tareas")
public class TaskController {

  @Autowired private TaskService taskService;

  // Obtener todas las tareas
  @GetMapping
  @Operation(
      summary = "Obtener todas las tareas",
      description = "Devuelve una lista de tareas, independiente de los filtros.")
  public ResponseEntity<List<Task>> getAllTasks() {
    List<Task> tasks = taskService.findAll();
    return ResponseEntity.ok(tasks);
  }

  // Obtener una tarea por ID
  @GetMapping("/{id}")
  @Operation(
      summary = "Obtener una tarea por ID",
      description = "Devuelve una tarea específica según el ID proporcionado.")
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
  @Operation(
      summary = "Crear una nueva tarea",
      description = "Crea una nueva tarea con los datos enviados en el cuerpo de la solicitud.")
  public ResponseEntity<Task> addNewTask(@RequestBody Task newTask) throws Exception {
    Task createdTask = taskService.addTask(newTask);

    HttpHeaders headers = new HttpHeaders();
    headers.set("location", String.valueOf(createdTask.getID_Task()));
    headers.set("Access-Control-Expose-Headers", "location");

    return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(createdTask);
  }

  // Actualizar una tarea existente
  @PutMapping("/{id}")
  @Operation(
      summary = "Actualizar una tarea existente",
      description = "Actualiza los datos de una tarea específica basado en su ID.")
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
  @Operation(
      summary = "Eliminar una tarea",
      description = "Elimina una tarea del según el ID proporcionado.")
  public ResponseEntity<Void> deleteTask(@PathVariable int id) {
    try {
      taskService.deleteTask(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
