package com.springboot.MyTodoList.controller;

import java.util.List;

import com.springboot.MyTodoList.model.Project;
import com.springboot.MyTodoList.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para gestionar operaciones CRUD de proyectos
 */
@RestController
@RequestMapping("/project")
@Tag(name = "Proyectos", description = "Operaciones CRUD para la entidad Proyecto")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    @Operation(
        summary = "Obtener todos los proyectos",
        description = "Devuelve una lista con todos los proyectos registrados."
    )
    public List<Project> getAllProjects() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener un proyecto por ID",
        description = "Devuelve un proyecto específico según el ID proporcionado."
    )
    public ResponseEntity<Project> getProjectById(
            @Parameter(description = "ID del proyecto", required = true)
            @PathVariable int id) {
        try {
            ResponseEntity<Project> responseEntity = projectService.getItemById(id);
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @Operation(
        summary = "Crear un nuevo proyecto",
        description = "Crea un nuevo proyecto con los datos enviados en el cuerpo de la solicitud."
    )
    public ResponseEntity<Void> addNewProject(
            @Parameter(description = "Datos del nuevo proyecto", required = true)
            @RequestBody Project new_project) throws Exception {
        Project project = projectService.addProject(new_project);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + project.getID_Project());
        responseHeaders.set("Access-Control-Expose-Headers", "location");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar un proyecto existente",
        description = "Actualiza los datos de un proyecto específico basado en su ID."
    )
    public ResponseEntity<Project> updateProject(
            @Parameter(description = "Datos actualizados del proyecto", required = true)
            @RequestBody Project project,
            @Parameter(description = "ID del proyecto a actualizar", required = true)
            @PathVariable int id) {
        try {
            Project updatedProject = projectService.updateProject(id, project);
            return new ResponseEntity<>(updatedProject, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar un proyecto",
        description = "Elimina un proyecto del sistema según el ID proporcionado."
    )
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "ID del proyecto a eliminar", required = true)
            @PathVariable int id) {
        try {
            projectService.deleteProject(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
