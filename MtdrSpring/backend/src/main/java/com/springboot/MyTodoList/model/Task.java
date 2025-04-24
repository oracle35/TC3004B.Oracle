package com.springboot.MyTodoList.model;

import java.time.OffsetDateTime;
import javax.persistence.*;

@Entity
@Table(name = "TASKS")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ID_Task;

    @Column(name = "DESCRIPTION")
    String description;

    @Column(name = "STATE")
    String state;

    @Column(name = "HOURS_ESTIMATED")
    Integer hoursEstimated;

    @Column(name = "HOURS_REAL")
    Integer hoursReal = 0; // Default value set to 0

    @Column(name = "ID_SPRINT")
    int ID_Sprint;

    @Column(name = "ASSIGNED_TO")
    Long assignedTo;

    @Column(name = "CREATED_AT")
    OffsetDateTime createdAt;

    @Column(name = "FINISHES_AT")
    OffsetDateTime finishesAt;

    @Column(name = "UPDATED_AT")
    OffsetDateTime updatedAt;

    public Task() {
        this.hoursReal = 0; // Ensure default value is set in the default constructor
    }

    public Task(
            int ID_Task,
            String description,
            String state,
            int hoursEstimated,
            int hoursReal,
            int ID_Sprint,
            int assignedTo,
            OffsetDateTime createdAt,
            OffsetDateTime finishesAt,
            OffsetDateTime updatedAt) {
        this.ID_Task = ID_Task;
        this.description = description;
        this.state = state;
        this.hoursEstimated = hoursEstimated;
        this.hoursReal = hoursReal;
        this.ID_Sprint = ID_Sprint;
        this.assignedTo = assignedTo;
        this.createdAt = createdAt;
        this.finishesAt = finishesAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters

    public int getID_Task() {
        return ID_Task;
    }

    public void setID_Task(int ID_Task) {
        this.ID_Task = ID_Task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getHoursEstimated() {
        return hoursEstimated;
    }

    public void setHoursEstimated(Integer hoursEstimated) {
        this.hoursEstimated = hoursEstimated;
    }

    public Integer getHoursReal() {
        return hoursReal;
    }

    public void setHoursReal(Integer hoursReal) {
        this.hoursReal = hoursReal;
    }

    public int getID_Sprint() {
        return ID_Sprint;
    }

    public void setID_Sprint(int ID_Sprint) {
        this.ID_Sprint = ID_Sprint;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getFinishesAt() {
        return finishesAt;
    }

    public void setFinishesAt(OffsetDateTime finishesAt) {
        this.finishesAt = finishesAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Task{"
                + "ID_Task="
                + ID_Task
                + ", description='"
                + description
                + '\''
                + ", state='"
                + state
                + '\''
                + ", hoursEstimated="
                + hoursEstimated
                + ", hoursReal="
                + hoursReal
                + ", ID_Sprint="
                + ID_Sprint
                + ", assignedTo="
                + assignedTo
                + ", startsAt="
                + createdAt
                + ", endsAt="
                + finishesAt
                + ", updatedAt="
                + updatedAt
                + '}';
    }
}
