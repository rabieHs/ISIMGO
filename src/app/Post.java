package app;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private static int nextId = 1;
    private int id;
    private User user;
    private String content;
    private List<Comment> comments;
    private int likes;

    public Post(User user, String content) {
        this.id = nextId++;
        this.user = user;
        this.content = content;
        this.comments = new ArrayList<>();
        this.likes = 0;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", user=" + user +
                ", content='" + content + '\'' +
                ", comments=" + comments +
                ", likes=" + likes +
                '}';
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public int getLikes() {
        return likes;
    }

    public void addLike() {
        likes++;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }
}
