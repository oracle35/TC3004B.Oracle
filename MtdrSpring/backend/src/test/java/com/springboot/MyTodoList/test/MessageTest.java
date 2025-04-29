package com.springboot.MyTodoList.test;

import com.springboot.MyTodoList.model.Message;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    @Test
    public void testMessageConstructorAndGetters() {
        int id = 1;
        String content = "Hello World";
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 10, 30);
        int userId = 42;

        Message message = new Message(id, content, timestamp, userId);

        assertEquals(id, message.getID_Message());
        assertEquals(content, message.getContent());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals(userId, message.getUserId());
    }

    @Test
    public void testMessageSetters() {
        Message message = new Message();

        int id = 2;
        String content = "Another test";
        LocalDateTime timestamp = LocalDateTime.of(2025, 4, 29, 12, 0);
        int userId = 99;

        message.setID_Message(id);
        message.setContent(content);
        message.setTimestamp(timestamp);
        message.setUserId(userId);

        assertEquals(id, message.getID_Message());
        assertEquals(content, message.getContent());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals(userId, message.getUserId());
    }

    @Test
    public void testMessageToString() {
        Message message = new Message(3, "JUnit Test", LocalDateTime.of(2025, 4, 29, 16, 45), 100);
        String toStringResult = message.toString();

        assertTrue(toStringResult.contains("ID_Message=3"));
        assertTrue(toStringResult.contains("content='JUnit Test'"));
        assertTrue(toStringResult.contains("timestamp=2025-04-29T16:45"));
        assertTrue(toStringResult.contains("userId=100"));
    }
}
