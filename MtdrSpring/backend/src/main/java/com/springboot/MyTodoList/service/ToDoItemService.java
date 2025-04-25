package com.springboot.MyTodoList.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.repository.ToDoItemRepository;

@Service
public class ToDoItemService {

  @Autowired private ToDoItemRepository toDoItemRepository;

  public List<ToDoItem> findAll() {
    List<ToDoItem> todoItems = toDoItemRepository.findAll();
    return todoItems;
  }

  public ResponseEntity<ToDoItem> getItemById(int id) {
    Optional<ToDoItem> todoData = toDoItemRepository.findById(id);
    if (todoData.isPresent()) {
      return new ResponseEntity<>(todoData.get(), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  public ToDoItem addToDoItem(ToDoItem toDoItem) {
    return toDoItemRepository.save(toDoItem);
  }

  public boolean deleteToDoItem(int id) {
    try {
      toDoItemRepository.deleteById(id);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public ToDoItem updateToDoItem(int id, ToDoItem td) {
    Optional<ToDoItem> toDoItemData = toDoItemRepository.findById(id);
    if (toDoItemData.isPresent()) {
      ToDoItem toDoItem = toDoItemData.get();
      toDoItem.setID_Task(id);
      toDoItem.setDescription(td.getDescription());
      toDoItem.setState(td.getState());
      toDoItem.setHoursEstimated(td.getHoursEstimated());
      toDoItem.setHoursReal(td.getHoursReal());
      toDoItem.setAssignedTo(td.getAssignedTo());
      toDoItem.setID_Sprint(td.getID_Sprint());
      toDoItem.setCreatedAt(td.getCreatedAt());
      toDoItem.setFinishesAt(td.getFinishesAt());
      toDoItem.setUpdatedAt(td.getUpdatedAt());
      return toDoItemRepository.save(toDoItem);
    } else {
      return null;
    }
  }
}
