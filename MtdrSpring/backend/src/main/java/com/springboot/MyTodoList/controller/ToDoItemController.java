package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.service.ToDoItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST Controller for managing To-Do items.
 * Provides CRUD operations for To-Do items.
 */
@RestController
@RequestMapping("/todolist")
@Tag(name = "To-Do Items", description = "API para la gestión de tareas pendientes")
public class ToDoItemController {
    @Autowired private ToDoItemService toDoItemService;

    /**
     * Obtiene todas las tareas pendientes.
     *
     * @return Lista de todas las tareas
     */
    @Operation(
            summary = "Obtener todas las tareas",
            description = "Recupera todas las tareas pendientes almacenadas en el sistema")
    @ApiResponse(
            responseCode = "200",
            description = "Tareas recuperadas exitosamente",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ToDoItem.class)))
    @GetMapping
    public List<ToDoItem> getAllToDoItems() {
        return toDoItemService.findAll();
    }

    /**
     * Obtiene una tarea por su ID.
     *
     * @param id ID de la tarea a buscar
     * @return La tarea si se encuentra, o respuesta 404 si no existe
     */
    @Operation(
            summary = "Obtener tarea por ID",
            description = "Busca y devuelve una tarea específica según su ID")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Tarea encontrada",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ToDoItem.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Tarea no encontrada",
                        content = @Content)
            })
    @GetMapping("/{id}")
    public ResponseEntity<ToDoItem> getToDoItemById(
            @Parameter(description = "ID de la tarea a buscar", required = true) @PathVariable
                    int id) {
        try {
            ResponseEntity<ToDoItem> responseEntity = toDoItemService.getItemById(id);
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Crea una nueva tarea.
     *
     * @param todoItem La tarea a crear
     * @return Respuesta con la ubicación de la nueva tarea en los headers
     * @throws Exception Si ocurre un error al crear la tarea
     */
    @Operation(
            summary = "Crear nueva tarea",
            description = "Registra una nueva tarea en el sistema")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Tarea creada correctamente",
                        content = @Content),
                @ApiResponse(
                        responseCode = "400",
                        description = "Datos de tarea inválidos",
                        content = @Content)
            })
    @PostMapping
    public ResponseEntity<ToDoItem> addToDoItem(
            @Parameter(
                            description = "Información de la tarea a crear",
                            required = true,
                            schema = @Schema(implementation = ToDoItem.class))
                    @RequestBody
                    ToDoItem todoItem)
            throws Exception {
        ToDoItem td = toDoItemService.addToDoItem(todoItem);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + td.getID_Task());
        responseHeaders.set("Access-Control-Expose-Headers", "location");
        // URI location = URI.create(""+td.getID())

        return ResponseEntity.ok().headers(responseHeaders).build();
    }

    /**
     * Actualiza una tarea existente.
     *
     * @param toDoItem Nueva información para la tarea
     * @param id       ID de la tarea a actualizar
     * @return La tarea actualizada o respuesta 404 si no existe
     */
    @Operation(
            summary = "Actualizar tarea",
            description = "Actualiza la información de una tarea existente")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Tarea actualizada correctamente",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ToDoItem.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Tarea no encontrada",
                        content = @Content)
            })
    @PutMapping("/{id}")
    public ResponseEntity<ToDoItem> updateToDoItem(
            @Parameter(
                            description = "Nueva información para la tarea",
                            required = true,
                            schema = @Schema(implementation = ToDoItem.class))
                    @RequestBody
                    ToDoItem toDoItem,
            @Parameter(description = "ID de la tarea a actualizar", required = true) @PathVariable
                    int id) {
        try {
            ToDoItem toDoItem1 = toDoItemService.updateToDoItem(id, toDoItem);
            return ResponseEntity.ok(toDoItem1);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Elimina una tarea por su ID.
     *
     * @param id ID de la tarea a eliminar
     * @return true si se eliminó correctamente, o respuesta 404 si no existe
     */
    @Operation(
            summary = "Eliminar tarea",
            description = "Elimina una tarea del sistema según su ID")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Tarea eliminada correctamente",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = Boolean.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Tarea no encontrada",
                        content = @Content)
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteToDoItem(
            @Parameter(description = "ID de la tarea a eliminar", required = true)
                    @PathVariable("id")
                    int id) {
        Boolean flag = false;
        try {
            flag = toDoItemService.deleteToDoItem(id);
            return new ResponseEntity<>(flag, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(flag, HttpStatus.NOT_FOUND);
        }
    }
}
