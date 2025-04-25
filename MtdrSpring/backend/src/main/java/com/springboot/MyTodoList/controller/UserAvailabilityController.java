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

/*
 * TODO: Add Swagger documentation.
 * */

@RestController
@RequestMapping("/userAvailability")
@Tag(
    name = "User Availability",
    description = "Operaciones CRUD para la tabla de Disponibilidad de Usuario")
public class UserAvailabilityController {
  @Autowired private UserAvailabilityService userAvailabilityService;

  @GetMapping
  public List<UserAvailability> getUserAvailability() {
    return userAvailabilityService.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserAvailability> getUserAvailabilityById(
      @Parameter(description = "ID del Usuario", required = true) @PathVariable int id) {
    try {
      ResponseEntity<UserAvailability> responseEntity =
          userAvailabilityService.getUserAvailabilityById(id);
      return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping
  public ResponseEntity<Void> addNewUserAvailability(
      @Parameter(description = "Datos de la nueva disponibilidad de usuario", required = true)
          @RequestBody
          UserAvailability userAvailabilityBody) {
    UserAvailability userAvailability =
        userAvailabilityService.addUserAvailability(userAvailabilityBody);
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("location", "" + userAvailability.getID_USER());
    responseHeaders.set("Access-Control-Expose-Headers", "location");
    return ResponseEntity.ok().headers(responseHeaders).build();
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Actualizar una disponibilidad de Usuario.",
      description = "Actualiza la disponibilidad del usuario.")
  public ResponseEntity<UserAvailability> updateUserAvailability(
      @RequestBody UserAvailability userAvailabilityBody, @PathVariable int id) {
    try {
      UserAvailability updatedAvailability =
          userAvailabilityService.updateUserAvailability(userAvailabilityBody);
      return new ResponseEntity<>(updatedAvailability, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUserAvailability(@PathVariable int id) {
    try {
      userAvailabilityService.deleteUserAvailabilityById(id);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
