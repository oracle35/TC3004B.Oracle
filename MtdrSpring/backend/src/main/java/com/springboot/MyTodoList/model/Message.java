package com.springboot.MyTodoList.model;

import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "MESSAGES")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID_Message;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "POSTED_AT")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "NOTIFICATE_TO")
    private int userId;

    public Message() {}

    public Message(int ID_Message, String content, LocalDateTime timestamp, int userId) {
        this.ID_Message = ID_Message;
        this.content = content;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public int getID_Message() {
        return ID_Message;
    }

    public void setID_Message(int ID_Message) {
        this.ID_Message = ID_Message;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Message{"
                + "ID_Message="
                + ID_Message
                + ", content='"
                + content
                + '\''
                + ", timestamp="
                + timestamp
                + ", userId="
                + userId
                + '}';
    }
}
