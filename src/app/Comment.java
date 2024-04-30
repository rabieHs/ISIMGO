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

    // Getters
}
