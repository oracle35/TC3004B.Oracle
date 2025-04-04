package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Message;
import com.springboot.MyTodoList.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @GetMapping
    public List<Message> getAllMessages() {
        return messageService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable int id) {
        return messageService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody Message message) {
        return ResponseEntity.ok(messageService.addMessage(message));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable int id, @RequestBody Message message) {
        Message updated = messageService.updateMessage(id, message);
        if (updated != null) return ResponseEntity.ok(updated);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable int id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok().build();
    }
}
