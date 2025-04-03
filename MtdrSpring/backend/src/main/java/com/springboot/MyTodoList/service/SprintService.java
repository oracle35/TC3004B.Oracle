package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SprintService {
    @Autowired
    private SprintRepository sprintRepository;

    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    public ResponseEntity<Sprint> getItemById(int id) {
        Optional<Sprint> sprintItems = sprintRepository.findById(id);
        return sprintItems.map(sprint -> new ResponseEntity<>(sprint, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public Sprint addSprint(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    public void deleteSprint(int id) {
        try {
            sprintRepository.deleteById(id);
        } catch (Exception ignored) {
        }
    }

    public Sprint updateSprint(int id, Sprint newSprint) {
        Optional<Sprint> sprintData = sprintRepository.findById(id);
        if (sprintData.isPresent()) {
            Sprint sprint_to_be_updated = sprintData.get();
            sprint_to_be_updated.setID_Sprint(id);
            sprint_to_be_updated.setName(newSprint.getName());
            sprint_to_be_updated.setStartsAt(newSprint.getStartsAt());
            sprint_to_be_updated.setEndsAt(newSprint.getEndsAt());
            return sprintRepository.save(sprint_to_be_updated);
        } else {
            return null;
        }
    }
}
