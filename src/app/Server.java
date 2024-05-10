package app;

import DAO.ConnectionDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server extends Thread {
    private List<User> users;
    private BlockingQueue<Message> messageQueue;
    private ConnectionDB connection;

    public Server() {
        users = new ArrayList<>();
        this.messageQueue = new LinkedBlockingQueue<>();
        connection=ConnectionDB.getInstance();
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
            this.connection.getConnection();
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
        // Prepare SQL statement
        String sql = "INSERT INTO users (name, email, password,status) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, password);
            statement.setString(4,"offline");

            // Execute the statement
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 1) {
                // User successfully registered
                return true;
            } else {
                // Registration failed
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

/*    public synchronized User authenticateUser(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user; // User found, authentication successful
            }
        }
        return null; // User not found or password incorrect
    }*/
public synchronized User authenticateUser(String email, String password,Socket socket) {
    // Prepare SQL statement
    String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

    try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
        statement.setString(1, email);
        statement.setString(2, password);

        // Execute the statement
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                // User found, authentication successful
                String name = resultSet.getString("name");
                int id=resultSet.getInt("userid");
                User user=new User(id,name, email, password, socket);
                List<Post> posts=getUserPosts(user);
                for(Post post:posts){
                    user.addPost(post);
                }
                users.add(user);
                // Create a new User object and return it

                return user;
            } else {
                // User not found or password incorrect
                return null;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
}
    public synchronized void showLoggedInUsers() {
        for (User user : users) {
            if (user.getSocket() != null) {
                System.out.println("User logged in: " + user.getName() + " (" + user.getEmail() + ")");
            }
        }
    }
    public synchronized User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE userid = ?";
        try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
            statement.setInt(1, userId);

            // Execute the statement
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Create a new User object for the row in the result set
                    String name = resultSet.getString("name");
                    String email = resultSet.getString("email");
                    String password = resultSet.getString("password");

                    return new User(userId, name, email, password,null);
                } else {
                    System.out.println("user not found");
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized void addPost(User user, String content) {
        // Prepare SQL statement
        String sql = "INSERT INTO posts (user, content, createdAt) VALUES (?, ?, NOW())";

        try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
            statement.setInt(1, user.getId());
            statement.setString(2, content);
            // Execute the statement

            if(statement.executeUpdate()==1){
                String sqlPost = "SELECT * FROM posts WHERE user=?";
                try (PreparedStatement statementPost = connection.getConnection().prepareStatement(sqlPost)){
                    statementPost.setInt(1, user.getId());
                    ResultSet result=statementPost.executeQuery();
                    if(result.next()){
                        int postId = result.getInt("postid");
                        // Add the post to the user's list of posts
                        Timestamp now =Timestamp.valueOf(LocalDateTime.now());
                        Post post=new Post(postId,user,content,now);

                        user.addPost(post);
                    }
                }catch (SQLException e){
                    e.printStackTrace();
                }


            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public synchronized  List<Post> getUserPosts(User user){
        List<Post> posts=new ArrayList<>();
        String sql = "SELECT * FROM posts WHERE user = ?";
        try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
            statement.setInt(1, user.getId());

            // Execute the statement
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Create a new Post object for each row in the result set
                    int id = resultSet.getInt("postid");
                    String content = resultSet.getString("content");
                    Timestamp createdAt = resultSet.getTimestamp("createdAt");
                    int likes=resultSet.getInt("likes");
                    Post post=new Post(id,user, content, createdAt);
                    List<Comment> postComments=getComments(post);
                    for(Comment comment: postComments){
                        post.addComment(comment);
                        for(Like like:getLikes(post)){
                            post.addLike(like);
                        }
                    }
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return posts;
    }
    public synchronized List<Post> getPosts() {
        List<Post> allPosts = new ArrayList<>();
        for (User user : users) {
            allPosts.addAll(user.getPosts());
        }
        return allPosts;
    }
    public synchronized List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM posts ORDER BY createdAt DESC";
        try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
            // Execute the statement
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Create a new Post object for each row in the result set
                    int id = resultSet.getInt("postid");
                    String content = resultSet.getString("content");
                    Timestamp createdAt = resultSet.getTimestamp("createdAt");
                    int userId = resultSet.getInt("user");
                    // Get the user who created the post
                    User user = getUserById(userId);

                    // Create a new Post object and add it to the list
                    Post post = new Post(id,user, content, createdAt );
                    List <Comment> comments=getComments(post);
                    for(Comment comment:comments){
                        post.addComment(comment);
                        for(Like like:getLikes(post)){
                            post.addLike(like);
                        }
                    }

                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return posts;
    }
    public synchronized void addComment( Post post, User user,String content) {
        // Prepare SQL statement
        String sql = "INSERT INTO comments (postid, user, content, createdAt) VALUES (?, ?, ?, NOW())";

        try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
            statement.setInt(1, post.getId());
            statement.setInt(2, user.getId());
            statement.setString(3, content);
            // Execute the statement
            if(statement.executeUpdate()==1){
                String sqlcomment = "SELECT * FROM comments ORDER BY createdAt DESC LIMIT 1";
                try (PreparedStatement statementComment = connection.getConnection().prepareStatement(sqlcomment)){
                    ResultSet result=statementComment.executeQuery();
                    if(result.next()){
                        int commentId = result.getInt("commentid");
                        Timestamp createdAt = result.getTimestamp("createdAt");
                        // Add the comment to the post's list of comments
                        int commentid= result.getInt("commentid");
                        System.out.println("comment craeted with success by,"+user.getName());
                        post.addComment(new Comment(commentid,user,content,createdAt));
                    }
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public synchronized List<Comment> getComments(Post post) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comments WHERE postid = ?";
        try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
            statement.setInt(1, post.getId());

            // Execute the statement
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Create a new Comment object for each row in the result set
                    int id = resultSet.getInt("commentid");
                    String content = resultSet.getString("content");
                    Timestamp createdAt = resultSet.getTimestamp("createdAt");
                    int userId = resultSet.getInt("user");

                    // Get the user who created the comment
                    User user = getUserById(userId);

                    Comment comment = new Comment(id,user, content, createdAt);
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return comments;
    }

    public synchronized boolean addLike(Post post, User user) {
        // Prepare SQL statement
        String sql = "INSERT INTO likes (postid, user) VALUES (?, ?)";

        try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
            statement.setInt(1, post.getId());
            statement.setInt(2, user.getId());

            // Execute the statement

            if(statement.executeUpdate()==1){
                // Add the like to the post's list of likes
                post.addLike(new Like(user,post.getId()));
                return true;
            }else{
                return  false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public synchronized List<Like> getLikes(Post post) {
        List<Like> likes = new ArrayList<>();
        String sql = "SELECT * FROM likes WHERE postid = ?";
        try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
            statement.setInt(1, post.getId());

            // Execute the statement
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // Create a new Like object for each row in the result set
                    int userId = resultSet.getInt("user");

                    // Get the user who liked the post
                    User user = getUserById(userId);

                    Like like = new Like(user,post.getId());
                    System.out.println(like.toString());
                    likes.add(like);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return likes;
    }
}
