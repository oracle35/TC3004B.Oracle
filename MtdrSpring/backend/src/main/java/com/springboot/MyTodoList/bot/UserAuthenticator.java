package com.springboot.MyTodoList.bot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.springboot.MyTodoList.model.User;
import com.springboot.MyTodoList.service.UserService;

public class UserAuthenticator {
  private final UserService userService;
  private final Logger logger = LoggerFactory.getLogger(UserAuthenticator.class);
  private final Map<Long, Optional<User>> userCache = new HashMap<>();

  private void initCache() {
    this.userService.findAll().stream()
        .forEach(
            user -> {
              // Some have no telegram ID yet
              if (user.getID_Telegram() == null) return;
              userCache.put(user.getID_Telegram(), Optional.of(user));
            });
  }

  public UserAuthenticator(UserService userService) {
    this.userService = userService;
    initCache();
  }

  /**
   * This function checks that there exists a User on the
   * database with the sender's telegram ID. Its presence
   * determines if the user is authenticated or not.
   */
  public Optional<User> authenticate(Long senderId) {
    if (userCache.containsKey(senderId)) {
      return userCache.get(senderId);
    }

    logger.info("Authenticating " + senderId + "...");
    Optional<User> queryResult =
      userService.findAll().stream()
        .filter(user -> user.getID_Telegram() == senderId)
        .findFirst();

    // NOTE: If a user sends a message and is not found on the DB
    // the cache will mark them as an unauthenticated user indefinitely.
    // If the user is registered without restarting the bot, they will still
    // be locked out of authenticated commands.
    // TODO: implement a mechanism to force a DB query and "refresh" a user's
    // status on the cache (also prevent abuse?)
    userCache.put(senderId, queryResult);
    return queryResult;
  }
}

