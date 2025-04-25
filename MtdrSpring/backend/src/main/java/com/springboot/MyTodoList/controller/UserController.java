package com.springboot.MyTodoList.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
  @Autowired private UserService userService;

  @GetMapping
  public List<User> getAllUser() {
    return userService.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable int id) {
    try {
      ResponseEntity<User> responseEntity = userService.getUserById(id);
      return new ResponseEntity<User>(responseEntity.getBody(), HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping
  public ResponseEntity addUser(@RequestBody User new_user) throws Exception {
    User user = userService.addUser(new_user);
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set("location", "" + user.getID_User());
    responseHeaders.set("Access-Control-Expose-Headers", "location");

    return ResponseEntity.ok().headers(responseHeaders).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity updateUser(@RequestBody User user, @PathVariable int id) {
    try {
      User user1 = userService.updateUser(id, user);
      System.out.println(user1.toString());
      return new ResponseEntity<>(user1, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
  }

  // In theory id should be passed through the body, but for simplicity it is passed through the
  // URL.
  @DeleteMapping("/{id}")
  public ResponseEntity deleteUser(@PathVariable int id) {
    try {
      userService.deleteUser(id);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
