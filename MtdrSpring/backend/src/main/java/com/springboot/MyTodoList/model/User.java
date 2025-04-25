package com.springboot.MyTodoList.model;

import javax.persistence.*;

@Entity
@Table(name="USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID_User;

    @Column(name="ID_TELEGRAM")
    private Long id_Telegram;

    @Column(name="NAME")
    private String name;

    @Column(name="POSITION")
    private String position;

    public User() {
    }

    public User(int ID_User, Long id_Telegram, String name, String position) {
        this.ID_User = ID_User;
        this.id_Telegram = id_Telegram;
        this.name = name;
        this.position = position;
    }

    public int getID_User() {
        return ID_User;
    }

    public void setID_User(int ID_User) {
        this.ID_User = ID_User;
    }

    public Long getID_Telegram() {
        return id_Telegram;
    }

    public void setID_Telegram(Long id_Telegram) {
        this.id_Telegram = id_Telegram;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "User{" +
                "ID_User=" + ID_User +
                ", id_Telegram='" + id_Telegram + '\'' +
                ", name='" + name + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}
