package app;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String status;
    private List<Post> posts;
   private Socket socket;



    public User(int id,String name, String email, String password ,Socket socket) {
        this.socket = socket; // Initialize socket

        this.id =id ;
        this.name = name;
        this.email = email;
        this.password = password;
        this.status="offline";
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

    public void addPost(Post post) {
        posts.add(post);
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
