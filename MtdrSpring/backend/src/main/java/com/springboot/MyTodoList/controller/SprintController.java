package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sprint")
public class SprintController {
    @Autowired
    private SprintService sprintService;

    @GetMapping
    public List<Sprint> getAllSprints() {
        return sprintService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable int id) {
        try {
            ResponseEntity<Sprint> responseEntity = sprintService.getItemById(id);
            return new ResponseEntity<Sprint>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity addNewSprint(@RequestBody Sprint new_sprint) throws Exception {
        Sprint sprint = sprintService.addSprint(new_sprint);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + sprint.getID_Project());
        responseHeaders.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity updateSprint(@RequestBody Sprint sprint, @PathVariable int id) {
        try {
            Sprint sprint1 = sprintService.updateSprint(id, sprint);
            System.out.println(sprint1.toString());
            return new ResponseEntity<>(sprint1, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteSprint(@PathVariable int id) {
        try {
            sprintService.deleteSprint(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
