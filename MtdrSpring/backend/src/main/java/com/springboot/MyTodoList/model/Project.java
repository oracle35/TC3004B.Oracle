package com.springboot.MyTodoList.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="PROJECTS")
public class Project { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ID_Project;

    @Column(name = "NAME")
    String name;

    @Column(name = "DESCRIPTION")
    String description;

    public Project(int ID_Project, String name, String description) {
        this.ID_Project = ID_Project;
        this.name = name;
        this.description = description;
    }

    public int getID_Project() {
        return ID_Project;
    }
    public void setID_Project(int ID_Project) {
        this.ID_Project = ID_Project;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Project{" +
                "ID_Project=" + ID_Project +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}