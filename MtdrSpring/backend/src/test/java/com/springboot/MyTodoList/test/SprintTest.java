package com.springboot.MyTodoList.test;

import com.springboot.MyTodoList.model.Sprint;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SprintTest {

    @Test
    public void testSprintConstructorAndGetters() {
        int ID_Sprint = 1;
        String name = "Sprint 1";
        OffsetDateTime startsAt = OffsetDateTime.parse("2024-01-01T09:00:00+01:00");
        OffsetDateTime endsAt = OffsetDateTime.parse("2024-01-15T17:00:00+01:00");
        int ID_Project = 99;

        Sprint sprint = new Sprint(ID_Sprint, name, startsAt, endsAt, ID_Project);

        assertEquals(ID_Sprint, sprint.getID_Sprint());
        assertEquals(name, sprint.getName());
        assertEquals(startsAt, sprint.getStartsAt());
        assertEquals(endsAt, sprint.getEndsAt());
        assertEquals(ID_Project, sprint.getID_Project());
    }

    @Test
    public void testSprintSetters() {
        Sprint sprint = new Sprint();

        OffsetDateTime date = OffsetDateTime.now();

        sprint.setID_Sprint(2);
        sprint.setName("Sprint Test");
        sprint.setStartsAt(date);
        sprint.setEndsAt(date);
        sprint.setID_Project(101);

        assertEquals(2, sprint.getID_Sprint());
        assertEquals("Sprint Test", sprint.getName());
        assertEquals(date, sprint.getStartsAt());
        assertEquals(date, sprint.getEndsAt());
        assertEquals(101, sprint.getID_Project());
    }

    @Test
    public void testSprintToString() {
        Sprint sprint = new Sprint(3, "Final Sprint",
                OffsetDateTime.parse("2025-03-01T08:00:00+01:00"),
                OffsetDateTime.parse("2025-03-14T16:00:00+01:00"),
                5);

        String result = sprint.toString();

        assertTrue(result.contains("ID_Sprint=3"));
        assertTrue(result.contains("name='Final Sprint'"));
        assertTrue(result.contains("startsAt=2025-03-01T08:00"));
        assertTrue(result.contains("endsAt=2025-03-14T16:00"));
        assertTrue(result.contains("ID_Project=5"));
    }
}
