package app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server extends Thread {
    private List<User> users;
    private BlockingQueue<Message> messageQueue;


    public Server() {
        users = new ArrayList<>();
        this.messageQueue = new LinkedBlockingQueue<>();

    }
    public static void main(String[] args) {
        new Server().start();
    }


    @Override
    public void run() {
        ServerSocket serverSocket;
        Socket socket;
        try {
            serverSocket = new ServerSocket(8888);
            System.out.println("Server started");
            while (true) {
                socket = serverSocket.accept();
                System.out.println("A New Client connected with IP: " + socket.getRemoteSocketAddress());
                new Conversation(socket, users,this,messageQueue).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean registerUser(String name, String email, String password,Socket socket) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return false; // User with this email already exists
            }
        }
        users.add(new User(name, email, password,socket));
        return true;
    }

    public synchronized User authenticateUser(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user; // User found, authentication successful
            }
        }
        return null; // User not found or password incorrect
    }

    public synchronized void addPost(User user, String content) {
        user.addPost(content);
    }

    public synchronized List<Post> getPosts() {
        List<Post> allPosts = new ArrayList<>();
        for (User user : users) {
            allPosts.addAll(user.getPosts());
        }
        return allPosts;
    }


}
