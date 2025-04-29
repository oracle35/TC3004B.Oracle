package com.springboot.MyTodoList.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.springboot.MyTodoList.model.User;

public class UserTest {

    @Test
    public void testUserConstructorAndGetters() {
        int id = 1;
        Long telegramId = 123456789L;
        String name = "John Doe";
        String position = "Developer";
        User user = new User(id, telegramId, name, position);

        assertEquals(id, user.getID_User());
        assertEquals(telegramId, user.getID_Telegram());
        assertEquals(name, user.getName());
        assertEquals(position, user.getPosition());
    }

    @Test
    public void testUserSetters() {
        User user = new User();
        user.setID_User(2);
        user.setID_Telegram(987654321L);
        user.setName("Jane Smith");
        user.setPosition("Manager");

        assertEquals(2, user.getID_User());
        assertEquals(987654321L, user.getID_Telegram());
        assertEquals("Jane Smith", user.getName());
        assertEquals("Manager", user.getPosition());
    }

    @Test
    public void testUserToString() {
        User user = new User(3, 555666777L, "Alice", "Tester");
        String expected = "User{ID_User=3, id_Telegram='555666777', name='Alice', position='Tester'}";
        assertEquals(expected, user.toString());
    }
} 