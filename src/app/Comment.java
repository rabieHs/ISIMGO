package app;

import java.sql.Timestamp;

public class Comment {
    private int id;
    private User user;
    private String content;
    private Timestamp createdAt;

    public Comment(int id,User user, String content,Timestamp createdAt) {
        this.id = id;
        this.user = user;
        this.content = content;
        this.createdAt=createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
// Getters
}
