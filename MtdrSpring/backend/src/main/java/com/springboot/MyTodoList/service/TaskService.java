package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public ResponseEntity<Task> getItemById(int id) {
        Optional<Task> taskItems = taskRepository.findById(id);
        return taskItems.map(task -> new ResponseEntity<>(task, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public Task addTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(int id) {
        try {
            taskRepository.deleteById(id);
        } catch (Exception ignored) {
        }
    }

    public Task updateTask(int id, Task newTask) {
        Optional<Task> taskData = taskRepository.findById(id);
        if (taskData.isPresent()) {
            Task task_to_be_updated = taskData.get();
            task_to_be_updated.setID_Task(id);
            task_to_be_updated.setDescription(newTask.getDescription());
            task_to_be_updated.setState(newTask.getState());
            task_to_be_updated.setHoursEstimated(newTask.getHoursEstimated());
            task_to_be_updated.setHoursReal(newTask.getHoursReal());
            task_to_be_updated.setAssignedTo(newTask.getAssignedTo());
            task_to_be_updated.setID_Sprint(newTask.getID_Sprint());
            task_to_be_updated.setCreatedAt(newTask.getCreatedAt());
            task_to_be_updated.setFinishesAt(newTask.getFinishesAt());
            task_to_be_updated.setUpdatedAt(newTask.getUpdatedAt());
            return taskRepository.save(task_to_be_updated);
        } else {
            return null;
        }
    }
}
