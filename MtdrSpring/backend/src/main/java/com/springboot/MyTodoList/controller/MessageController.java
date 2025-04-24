package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Message;
import com.springboot.MyTodoList.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
@Tag(name = "Mensajes", description = "Operaciones relacionadas con mensajes")
public class MessageController {

    @Autowired private MessageService messageService;

    @GetMapping
    @Operation(
            summary = "Obtener todos los mensajes",
            description = "Retorna una lista con todos los mensajes existentes.")
    public List<Message> getAllMessages() {
        return messageService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un mensaje por ID",
            description = "Retorna un mensaje específico basado en su ID.")
    public ResponseEntity<Message> getMessageById(
            @Parameter(description = "ID del mensaje a buscar", required = true) @PathVariable
                    int id) {
        return messageService
                .findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
            summary = "Crear un nuevo mensaje",
            description = "Crea un nuevo mensaje con los datos proporcionados.")
    public ResponseEntity<Message> createMessage(
            @Parameter(description = "Objeto Message que será creado", required = true) @RequestBody
                    Message message) {
        return ResponseEntity.ok(messageService.addMessage(message));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un mensaje existente",
            description = "Actualiza un mensaje basado en el ID proporcionado.")
    public ResponseEntity<Message> updateMessage(
            @Parameter(description = "ID del mensaje a actualizar", required = true) @PathVariable
                    int id,
            @Parameter(description = "Datos actualizados del mensaje", required = true) @RequestBody
                    Message message) {
        Message updated = messageService.updateMessage(id, message);
        if (updated != null) return ResponseEntity.ok(updated);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un mensaje",
            description = "Elimina un mensaje según el ID proporcionado.")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "ID del mensaje a eliminar", required = true) @PathVariable
                    int id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok().build();
    }
}
