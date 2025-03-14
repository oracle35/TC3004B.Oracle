/**
 * Represents a ToDoItem entity mapped to the TODOITEM table in the database.
 * This class is used to store and retrieve information about to-do items.
 * 
 * Fields:
 * - ID: The unique identifier for the to-do item.
 * - description: A brief description of the to-do item.
 * - creation_ts: The timestamp when the to-do item was created.
 * - done: A boolean indicating whether the to-do item is completed.
 * 
 * Constructors:
 * - ToDoItem(): Default constructor.
 * - ToDoItem(int ID, String description, OffsetDateTime creation_ts, boolean done): Parameterized constructor.
 * 
 * Methods:
 * - getID(): Returns the ID of the to-do item.
 * - setID(int ID): Sets the ID of the to-do item.
 * - getDescription(): Returns the description of the to-do item.
 * - setDescription(String description): Sets the description of the to-do item.
 * - getCreation_ts(): Returns the creation timestamp of the to-do item.
 * - setCreation_ts(OffsetDateTime creation_ts): Sets the creation timestamp of the to-do item.
 * - isDone(): Returns whether the to-do item is completed.
 * - setDone(boolean done): Sets the completion status of the to-do item.
 * - toString(): Returns a string representation of the to-do item.
 */

package com.springboot.MyTodoList.model;


import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
@Entity
@Table(name = "TODOITEM")
public class ToDoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ID;
    @Column(name = "DESCRIPTION")
    String description;

    @Column(name = "DELIVERY_TS")
    OffsetDateTime delivery_ts;

    @Column(name = "CREATION_TS")
    OffsetDateTime creation_ts;
    @Column(name = "done")
    boolean done;
    public ToDoItem(){

    }
    public ToDoItem(int ID, String description, OffsetDateTime creation_ts, boolean done, OffsetDateTime delivery_ts) {
        this.ID = ID;
        this.description = description;
        this.creation_ts = creation_ts;
        this.done = done;
        this.delivery_ts = delivery_ts;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getCreation_ts() {
        return creation_ts;
    }

    public OffsetDateTime getDelivery_ts() {
        return delivery_ts;
    }

    public void setCreation_ts(OffsetDateTime creation_ts) {
        this.creation_ts = creation_ts;
    }

    public void setDelivery_ts(OffsetDateTime delivery_ts) {
        this.delivery_ts = delivery_ts;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "ID=" + ID +
                ", description='" + description + '\'' +
                ", creation_ts=" + creation_ts +
                ", delivery_ts=" + delivery_ts +
                ", done=" + done +
                '}';
    }
}
