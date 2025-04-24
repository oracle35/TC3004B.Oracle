package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public ResponseEntity<User> getUserById(int id) {
        return userRepository
                .findById(id)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }

    // TODO: Implement Exception Handling
    public void deleteUser(int id) {
        try {
            userRepository.deleteById(id);
        } catch (Exception ignored) {
        }
    }

    /**
     *
     * @param id: int
     * @param newUser: User
     * @return user updated
     */
    public User updateUser(int id, User newUser) {
        return userRepository
                .findById(id)
                .map(
                        user -> {
                            user.setID_User(id);
                            user.setID_Telegram(newUser.getID_Telegram());
                            user.setName(newUser.getName());
                            user.setPosition(newUser.getPosition());
                            return userRepository.save(user);
                        })
                .orElse(null);
    }
}
