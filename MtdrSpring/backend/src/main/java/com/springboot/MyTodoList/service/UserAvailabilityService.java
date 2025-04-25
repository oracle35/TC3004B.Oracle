package com.springboot.MyTodoList.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.model.UserAvailability;
import com.springboot.MyTodoList.repository.UserAvailabilityRepository;

@Service
public class UserAvailabilityService {
  @Autowired private UserAvailabilityRepository userAvailabilityRepository;

  public List<UserAvailability> findAll() {
    return userAvailabilityRepository.findAll();
  }

  public ResponseEntity<UserAvailability> getUserAvailabilityById(int ID_User) {
    Optional<UserAvailability> userAvailability = userAvailabilityRepository.findById(ID_User);
    return userAvailability
        .map(availability -> new ResponseEntity<>(availability, HttpStatus.OK))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  public UserAvailability addUserAvailability(UserAvailability userAvailability) {
    return this.userAvailabilityRepository.save(userAvailability);
  }

  public void deleteUserAvailabilityById(int ID_User) {
    userAvailabilityRepository.deleteById(ID_User);
  }

  public UserAvailability updateUserAvailability(UserAvailability userAvailability) {
    return userAvailabilityRepository.save(userAvailability);
  }
}
