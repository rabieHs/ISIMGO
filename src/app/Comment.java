package app;

public class Comment {
    private static int nextId = 1;
    private int id;
    private User user;
    private String content;

    public Comment(User user, String content) {
        this.id = nextId++;
        this.user = user;
        this.content = content;
    }

    public static int getNextId() {
        return nextId;
    }

    public static void setNextId(int nextId) {
        Comment.nextId = nextId;
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
