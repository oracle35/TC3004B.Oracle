package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestionar operaciones CRUD de sprints
 */
@RestController
@RequestMapping("/sprint")
@Tag(name = "Sprints", description = "Operaciones CRUD para la entidad Sprint")
public class SprintController {

    @Autowired
    private SprintService sprintService;

    @GetMapping
    @Operation(
        summary = "Obtener todos los sprints",
        description = "Devuelve una lista de todos los sprints registrados."
    )
    public List<Sprint> getAllSprints() {
        return sprintService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener un sprint por ID",
        description = "Devuelve un sprint específico según el ID proporcionado."
    )
    public ResponseEntity<Sprint> getSprintById(
            @Parameter(description = "ID del sprint", required = true)
            @PathVariable int id) {
        try {
            ResponseEntity<Sprint> responseEntity = sprintService.getItemById(id);
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @Operation(
        summary = "Crear un nuevo sprint",
        description = "Crea un nuevo sprint con los datos enviados en el cuerpo de la solicitud."
    )
    public ResponseEntity<Void> addNewSprint(
            @Parameter(description = "Datos del nuevo sprint", required = true)
            @RequestBody Sprint new_sprint) throws Exception {
        Sprint sprint = sprintService.addSprint(new_sprint);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + sprint.getID_Project());
        responseHeaders.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.ok()
                .headers(responseHeaders).build();
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar un sprint existente",
        description = "Actualiza los datos de un sprint específico basado en su ID."
    )
    public ResponseEntity<Sprint> updateSprint(
            @Parameter(description = "Datos actualizados del sprint", required = true)
            @RequestBody Sprint sprint,
            @Parameter(description = "ID del sprint a actualizar", required = true)
            @PathVariable int id) {
        try {
            Sprint updatedSprint = sprintService.updateSprint(id, sprint);
            return new ResponseEntity<>(updatedSprint, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar un sprint",
        description = "Elimina un sprint del sistema según el ID proporcionado."
    )
    public ResponseEntity<Void> deleteSprint(
            @Parameter(description = "ID del sprint a eliminar", required = true)
            @PathVariable int id) {
        try {
            sprintService.deleteSprint(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
