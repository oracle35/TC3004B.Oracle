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

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable int id) {
        try {
            ResponseEntity<Task> responseEntity = taskService.getItemById(id);
            return new ResponseEntity<Task>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity addNewTask(@RequestBody Task new_task) throws Exception {
        Task task = taskService.addTask(new_task);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + task.getID_Task());
        responseHeaders.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(task);

    }

    @PutMapping("/{id}")
    public ResponseEntity updateTask(@RequestBody Task task, @PathVariable int id) {
        try {
            Task task1 = taskService.updateTask(id, task);
            System.out.println(task1.toString());
            return new ResponseEntity<>(task1, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTask(@PathVariable int id) {
        try {
            taskService.deleteTask(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
