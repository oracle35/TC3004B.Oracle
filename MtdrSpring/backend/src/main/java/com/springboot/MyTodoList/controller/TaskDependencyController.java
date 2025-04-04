package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.TaskDependency;
import com.springboot.MyTodoList.repository.TaskDependencyRepository;
import com.springboot.MyTodoList.service.TaskDependencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taskDependency")
public class TaskDependencyController {
    @Autowired
    private TaskDependencyService taskDependencyService;

    @GetMapping
    public List<TaskDependency> getAllTaskDependencies() {
        return taskDependencyService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDependency> getTaskDependencyById(@PathVariable int id) {
        try {
            ResponseEntity<TaskDependency> responseEntity = taskDependencyService.getItemById(id);
            return new ResponseEntity<TaskDependency>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity addNewTaskDependency(@RequestBody TaskDependency new_taskDependency) throws Exception {
        TaskDependency taskDependency = taskDependencyService.addTaskDependency(new_taskDependency);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + taskDependency.getID_Task_Parent());
        responseHeaders.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity updateTaskDependency(@RequestBody TaskDependency taskDependency, @PathVariable int id) {
        try {
            TaskDependency taskDependency1 = taskDependencyService.updateTaskDependency(id, taskDependency);
            System.out.println(taskDependency1.toString());
            return new ResponseEntity<>(taskDependency1, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTaskDependency(@PathVariable int id) {
        try {
            taskDependencyService.deleteTaskDependency(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
