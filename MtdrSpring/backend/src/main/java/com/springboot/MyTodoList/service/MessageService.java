package com.springboot.MyTodoList.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.model.Message;
import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.repository.MessageRepository;
import com.springboot.MyTodoList.repository.UserRepository;

@Service
public class MessageService {

  @Autowired private MessageRepository messageRepository;

  @Autowired private UserRepository userRepository;

  public List<Message> findAll() {
    return messageRepository.findAll();
  }

  public Optional<Message> findById(int id) {
    return messageRepository.findById(id);
  }

  public Message addMessage(Message message) {
    int userId = message.getUserId();
    if (userId == 0) {
      throw new RuntimeException("Debes enviar un userId válido (> 0).");
    }

    Optional<User> userOpt = userRepository.findById(userId);
    if (!userOpt.isPresent()) {
      throw new RuntimeException("No existe un Usuario con ID " + userId);
    }

    message.setTimestamp(LocalDateTime.now());

    return messageRepository.save(message);
  }

  public Message updateMessage(int id, Message updated) {
    return messageRepository
        .findById(id)
        .map(
            msg -> {
              msg.setContent(updated.getContent());

              msg.setTimestamp(LocalDateTime.now());

              int userId = updated.getUserId();
              if (userId != 0) {
                userRepository
                    .findById(userId)
                    .orElseThrow(
                        () -> new RuntimeException("No existe un Usuario con ID " + userId));
                msg.setUserId(userId);
              }

              return messageRepository.save(msg);
            })
        .orElse(null);
  }

  public void deleteMessage(int id) {
    messageRepository.deleteById(id);
  }
}
