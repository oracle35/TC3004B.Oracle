package com.springboot.MyTodoList.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.springboot.MyTodoList.model.UserAvailability;
import com.springboot.MyTodoList.service.UserAvailabilityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para gestionar la disponibilidad de usuarios
 */
@RestController
@RequestMapping("/userAvailability")
@Tag(
    name = "User Availability",
    description = "Operaciones CRUD para la tabla de Disponibilidad de Usuario")
public class UserAvailabilityController {

    @Autowired
    private UserAvailabilityService userAvailabilityService;

    @GetMapping
    @Operation(
        summary = "Obtener todas las disponibilidades de usuarios",
        description = "Devuelve una lista con todas las disponibilidades registradas en el sistema."
    )
    public List<UserAvailability> getUserAvailability() {
        return userAvailabilityService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener disponibilidad por ID",
        description = "Devuelve la disponibilidad de un usuario específica basada en su ID."
    )
    public ResponseEntity<UserAvailability> getUserAvailabilityById(
        @Parameter(description = "ID del Usuario", required = true)
        @PathVariable int id) {
        try {
            ResponseEntity<UserAvailability> responseEntity = userAvailabilityService.getUserAvailabilityById(id);
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    @Operation(
        summary = "Crear nueva disponibilidad",
        description = "Crea una nueva disponibilidad de usuario con los datos proporcionados."
    )
    public ResponseEntity<Void> addNewUserAvailability(
        @Parameter(description = "Datos de la nueva disponibilidad de usuario", required = true)
        @RequestBody UserAvailability userAvailabilityBody) {
        UserAvailability userAvailability = userAvailabilityService.addUserAvailability(userAvailabilityBody);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("location", "" + userAvailability.getID_USER());
        responseHeaders.set("Access-Control-Expose-Headers", "location");
        return ResponseEntity.ok().headers(responseHeaders).build();
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar disponibilidad de un usuario",
        description = "Actualiza la disponibilidad existente de un usuario especificado por su ID."
    )
    public ResponseEntity<UserAvailability> updateUserAvailability(
        @Parameter(description = "Datos actualizados de disponibilidad", required = true)
        @RequestBody UserAvailability userAvailabilityBody,
        @Parameter(description = "ID del Usuario", required = true)
        @PathVariable int id) {
        try {
            UserAvailability updatedAvailability = userAvailabilityService.updateUserAvailability(userAvailabilityBody);
            return new ResponseEntity<>(updatedAvailability, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar disponibilidad",
        description = "Elimina una disponibilidad de usuario según el ID proporcionado."
    )
    public ResponseEntity<Void> deleteUserAvailability(
        @Parameter(description = "ID del Usuario", required = true)
        @PathVariable int id) {
        try {
            userAvailabilityService.deleteUserAvailabilityById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
