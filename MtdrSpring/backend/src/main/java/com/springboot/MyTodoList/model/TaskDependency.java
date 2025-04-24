package com.springboot.MyTodoList.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TASK_DEPENDENCY")
public class TaskDependency {
    @Id
    @Column(name = "ID_TASK_PARENT")
    private int ID_Task_Parent;

    @Column(name = "ID_TASK_CHILDREN")
    private int ID_Task_Children;

    public TaskDependency() {}

    public TaskDependency(int ID_Task_Parent, int ID_Task_Children) {
        this.ID_Task_Parent = ID_Task_Parent;
        this.ID_Task_Children = ID_Task_Children;
    }

    public int getID_Task_Parent() {
        return ID_Task_Parent;
    }

    public void setID_Task_Parent(int ID_Task_Parent) {
        this.ID_Task_Parent = ID_Task_Parent;
    }

    public int getID_Task_Children() {
        return ID_Task_Children;
    }

    public void setID_Task_Children(int ID_Task_Children) {
        this.ID_Task_Children = ID_Task_Children;
    }

    @Override
    public String toString() {
        return "TaskDependency{"
                + "ID_Task_Parent="
                + ID_Task_Parent
                + ", ID_Task_Children="
                + ID_Task_Children
                + '}';
    }
}
