package app;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class User {
    private static int nextId = 1;
    private int id;
    private String name;
    private String email;
    private String password;
    private List<Post> posts;
   private Socket socket;



    public User(String name, String email, String password ,Socket socket) {
        this.socket = socket; // Initialize socket

        this.id = nextId++;
        this.name = name;
        this.email = email;
        this.password = password;
        this.posts = new ArrayList<>();

    }

    public List<Post> getPosts() {
        return posts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPost(String content) {
        posts.add(new Post(this, content));
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
