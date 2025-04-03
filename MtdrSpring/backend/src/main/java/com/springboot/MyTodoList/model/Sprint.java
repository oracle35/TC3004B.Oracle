package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name="SPRINTS")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ID_Sprint;

    @Column(name = "NAME")
    String name;

    @Column(name="STARTS_AT")
    OffsetDateTime startsAt;

    @Column(name="ENDS_AT")
    OffsetDateTime endsAt;

    // refer to project ID, it has the id of the project it belongs to, as a foreign key
    @Column(name="ID_PROJECT")
    int ID_Project;

    public Sprint() {
    }

    public Sprint(int ID_Sprint, String name, OffsetDateTime startsAt, OffsetDateTime endsAt, int ID_Project) {
        this.ID_Sprint = ID_Sprint;
        this.name = name;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.ID_Project = ID_Project;
    }

    public int getID_Sprint() {
        return ID_Sprint;
    }

    public void setID_Sprint(int ID_Sprint) {
        this.ID_Sprint = ID_Sprint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(OffsetDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public int getID_Project() {
        return ID_Project;
    }

    public void setID_Project(int ID_Project) {
        this.ID_Project = ID_Project;
    }

    @Override
    public String toString() {
        return "Sprint{" +
                "ID_Sprint=" + ID_Sprint +
                ", name='" + name + '\'' +
                ", startsAt=" + startsAt +
                ", endsAt=" + endsAt +
                ", ID_Project=" + ID_Project +
                '}';
    }
}
