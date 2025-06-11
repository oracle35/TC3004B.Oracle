package com.springboot.MyTodoList.test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.springboot.MyTodoList.model.Task;

public class Tasktest {

    @Test
    public void testTaskConstructorAndGetters() {
        int ID_Task = 1;
        String description = "Implement login";
        String state = "In Progress";
        int hoursEstimated = 5;
        int hoursReal = 3;
        int ID_Sprint = 2;
        int assignedTo = 4;
        OffsetDateTime createdAt = OffsetDateTime.parse("2024-01-10T10:15:30+01:00");
        OffsetDateTime finishesAt = OffsetDateTime.parse("2024-01-15T18:00:00+01:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2024-01-12T12:30:00+01:00");
        int storyPoints = 3;

        Task task = new Task(ID_Task, description, state, hoursEstimated, hoursReal, ID_Sprint, assignedTo, createdAt,
                finishesAt, updatedAt, storyPoints);

        assertEquals(ID_Task, task.getID_Task());
        assertEquals(description, task.getDescription());
        assertEquals(state, task.getState());
        assertEquals(hoursEstimated, task.getHoursEstimated());
        assertEquals(hoursReal, task.getHoursReal());
        assertEquals(ID_Sprint, task.getID_Sprint());
        assertEquals(assignedTo, task.getAssignedTo());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(finishesAt, task.getFinishesAt());
        assertEquals(updatedAt, task.getUpdatedAt());
        assertEquals(storyPoints, task.getStoryPoints());
    }

    @Test
    public void testTaskSetters() {
        Task task = new Task();

        OffsetDateTime now = OffsetDateTime.now();
        int storyPoints = 5; // Example value for story points

        task.setID_Task(10);
        task.setDescription("Test setter");
        task.setState("Pending");
        task.setHoursEstimated(8);
        task.setHoursReal(4);
        task.setID_Sprint(7);
        task.setAssignedTo(3);
        task.setCreatedAt(now);
        task.setFinishesAt(now);
        task.setUpdatedAt(now);
        task.setStoryPoint(storyPoints);

        assertEquals(10, task.getID_Task());
        assertEquals("Test setter", task.getDescription());
        assertEquals("Pending", task.getState());
        assertEquals(8, task.getHoursEstimated());
        assertEquals(4, task.getHoursReal());
        assertEquals(7, task.getID_Sprint());
        assertEquals(3, task.getAssignedTo());
        assertEquals(now, task.getCreatedAt());
        assertEquals(now, task.getFinishesAt());
        assertEquals(now, task.getUpdatedAt());
        assertEquals(storyPoints, task.getStoryPoints());
    }

    @Test
    public void testTaskToString() {
        int storyPoints = 8; // Add a value for storyPoints
        Task task = new Task(5, "Deploy app", "Done", 10, 9, 3, 1,
                OffsetDateTime.parse("2025-01-01T10:00:00+01:00"),
                OffsetDateTime.parse("2025-01-05T18:00:00+01:00"),
                OffsetDateTime.parse("2025-01-04T12:00:00+01:00"),
                storyPoints); // Add storyPoints argument

        String result = task.toString();
        assertTrue(result.contains("ID_Task=5"));
        assertTrue(result.contains("description='Deploy app'"));
        assertTrue(result.contains("state='Done'"));
        assertTrue(result.contains("hoursEstimated=10"));
        assertTrue(result.contains("hoursReal=9"));
        assertTrue(result.contains("ID_Sprint=3"));
        assertTrue(result.contains("assignedTo=1"));
        assertTrue(result.contains("startsAt=2025-01-01T10:00"));
        assertTrue(result.contains("endsAt=2025-01-05T18:00"));
        assertTrue(result.contains("updatedAt=2025-01-04T12:00"));
        assertTrue(result.contains("storyPoints=" + storyPoints));
    }
}
