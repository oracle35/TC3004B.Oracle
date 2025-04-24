package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.TaskDependency;
import com.springboot.MyTodoList.repository.TaskDependencyRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TaskDependencyService {
    @Autowired private TaskDependencyRepository taskDependencyRepository;

    public List<TaskDependency> findAll() {
        return taskDependencyRepository.findAll();
    }

    public ResponseEntity<TaskDependency> getItemById(int id) {
        Optional<TaskDependency> taskDependencyItems = taskDependencyRepository.findById(id);
        return taskDependencyItems
                .map(taskDependency -> new ResponseEntity<>(taskDependency, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public TaskDependency addTaskDependency(TaskDependency taskDependency) {
        return taskDependencyRepository.save(taskDependency);
    }

    public void deleteTaskDependency(int id) {
        try {
            taskDependencyRepository.deleteById(id);
        } catch (Exception ignored) {
        }
    }

    public TaskDependency updateTaskDependency(int id, TaskDependency newTaskDependency) {
        Optional<TaskDependency> taskDependencyData = taskDependencyRepository.findById(id);
        if (taskDependencyData.isPresent()) {
            TaskDependency taskDependency_to_be_updated = taskDependencyData.get();
            taskDependency_to_be_updated.setID_Task_Children(
                    newTaskDependency.getID_Task_Children());
            taskDependency_to_be_updated.setID_Task_Parent(newTaskDependency.getID_Task_Parent());
            return taskDependencyRepository.save(taskDependency_to_be_updated);
        } else {
            return null;
        }
    }
}
