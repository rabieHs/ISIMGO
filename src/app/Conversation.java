package app;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Conversation extends Thread {
    private Socket socket;
    private List<User> users;
    private Server server;
    private BlockingQueue<Message> messageQueue;


    public Conversation(Socket socket, List<User> users, Server server, BlockingQueue<Message> messageQueue) {
        this.socket = socket;
        this.users = users;
        this.server = server;
        this.messageQueue = messageQueue;

    }

    @Override
    public void run() {
        try {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter, true);

            printWriter.println("Welcome to the Social Media App!");
            printWriter.println("Commands:");
            printWriter.println("1: login");
            printWriter.println("2: register");
            printWriter.println("3: exit server");

            String messageClient = bufferedReader.readLine();

            switch (messageClient) {
                case "1":
                    printWriter.println("Enter your email and password separated by ';'");
                    String[] credentials = bufferedReader.readLine().split(";");
                    String email = credentials[0];
                    String password = credentials[1];
                    User authenticatedUser = server.authenticateUser(email, password);
                    if (authenticatedUser != null) {
                        printWriter.println("Login successful. Welcome, " + authenticatedUser.getName() + "!");
                        handleUserMenu(printWriter, bufferedReader, authenticatedUser);
                    } else {
                        printWriter.println("Invalid email or password!");
                        printWriter.println("Commands:");
                        printWriter.println("1: login");
                        printWriter.println("2: register");
                        printWriter.println("3: exit server");
                        messageClient = bufferedReader.readLine();
                    }
                    break;
                case "2":
                    printWriter.println("Enter your name, email, and password separated by ';'");
                    String[] registrationInfo = bufferedReader.readLine().split(";");
                    String name = registrationInfo[0];
                    email = registrationInfo[1];
                    password = registrationInfo[2];

                    boolean isRegistered = server.registerUser(name, email, password,socket);
                    if (isRegistered) {
                        printWriter.println("Registration successful. You can now login.");
                        User registredUser = server.authenticateUser(email, password);

                        handleUserMenu(printWriter, bufferedReader, registredUser);

                    } else {
                        printWriter.println("Registration failed. User with this email already exists.");
                    }
                    break;
                case "3":
                    // Handle exiting server
                    break;
                default:
                    printWriter.println("Invalid command!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUserMenu(PrintWriter printWriter, BufferedReader bufferedReader, User authenticatedUser) throws IOException {
        printWriter.println("User Menu:");
        printWriter.println("1: View online users");
        printWriter.println("2: View posts list");
        printWriter.println("3: Sign out");

        String userCommand = bufferedReader.readLine();

        switch (userCommand) {
            case "1":
                printAllUsers(printWriter,bufferedReader,authenticatedUser);                break;
            case "2":
                printWriter.println("Posts List:");
                List<Post> allPosts = server.getPosts();
                for (Post post : allPosts) {
                    printWriter.println("ID: " + post.getId() + " | User: " + post.getUser().getName() + " | Content: " + post.getContent() + " | Likes: " + post.getLikes() + " | Comments: " + post.getComments().size());
                }
                printWriter.println("Commands:");
                printWriter.println("4: Add post");
                printWriter.println("5: View post details");
                printWriter.println("6: Back to main menu");
                String postCommand = bufferedReader.readLine();
                switch (postCommand) {
                    case "4":
                        printWriter.println("Enter your post content:");
                        String postContent = bufferedReader.readLine();
                        server.addPost(authenticatedUser, postContent);
                        printWriter.println("Post added successfully!");
                        handleUserMenu(printWriter, bufferedReader, authenticatedUser);
                        break;
                    case "5":
                        printWriter.println("Enter the ID of the post you want to view:");
                        int postId = Integer.parseInt(bufferedReader.readLine());
                       viewPostDetails(printWriter,bufferedReader,postId,authenticatedUser);
                        break;
                    case "6":
                        handleUserMenu(printWriter, bufferedReader, authenticatedUser); // Go back to main menu
                        break;
                    default:
                        printWriter.println("Invalid command!");
                }
                break;
            case "3":
                printWriter.println("You have been signed out.");
                break;
            default:
                printWriter.println("Invalid command!");
                handleUserMenu(printWriter, bufferedReader, authenticatedUser); // Stay in the user menu
        }


    }

    private void printAllUsers(PrintWriter printWriter, BufferedReader bufferedReader, User authenticatedUser) throws IOException {
        printWriter.println("All Users:");
        for (User user : users) {
            if (!user.equals(authenticatedUser)) {
                printWriter.println("ID: " + user.getId() + " | Name: " + user.getName());
            }
        }
        printWriter.println("Commands:");
        printWriter.println("1: Start a conversation");
        printWriter.println("2: Back to main menu");

        String userCommand = bufferedReader.readLine();
        switch (userCommand) {
            case "1":
                startConversation(printWriter, bufferedReader, authenticatedUser);
                break;
            case "2":
                handleUserMenu(printWriter, bufferedReader, authenticatedUser); // Back to main menu
                break;
            default:
                printWriter.println("Invalid command!");
                printAllUsers(printWriter, bufferedReader, authenticatedUser); // Stay in the all users list
        }
    }

    private void startConversation(PrintWriter printWriter, BufferedReader bufferedReader, User authenticatedUser) throws IOException {
        printWriter.println("Enter the ID of the user you want to start a conversation with:");
        String userInput = bufferedReader.readLine();
        int receiverId;
        try {
            receiverId = Integer.parseInt(userInput);
        } catch (NumberFormatException e) {
            printWriter.println("Invalid user ID format. Please enter a valid integer ID.");
            return;
        }

        User receiver = findUserById(receiverId);
        if (receiver != null) {
            printWriter.println("Conversation started with " + receiver.getName() + ". Type 'exit' to end the conversation.");

            // Create a BufferedReader for the receiver's input stream
            BufferedReader receiverBufferedReader = new BufferedReader(new InputStreamReader(receiver.getSocket().getInputStream()));

            String message;
            while (true) {
                // Read messages from the sender
                message = bufferedReader.readLine();


                if (message.equalsIgnoreCase("exit")) {
                    printAllUsers(printWriter, bufferedReader, authenticatedUser);
                    break;
                }

                // Create a message object and add it to the message queue
                Message sentMessage = new Message(authenticatedUser, receiver, message);
                messageQueue.add(sentMessage);

                // Print the sent message to the sender

                printWriter.println("You: " + message);

                // Forward the message to the receiver
                PrintWriter receiverPrintWriter = new PrintWriter(receiver.getSocket().getOutputStream(), true);
                receiverPrintWriter.println(authenticatedUser.getName() + ": " + message);

                // Read messages from the receiver
                message = receiverBufferedReader.readLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // Create a message object and add it to the message queue
                Message receivedMessage = new Message(receiver, authenticatedUser, message);
                messageQueue.add(receivedMessage);

                // Print the received message to the sender
                printWriter.println(receiver.getName() + ": " + message);
            }
        } else {
            printWriter.println("User with ID " + receiverId + " not found.");
        }
    }


    private User findUserById(int userId) {
        for (User user : users) {
            if (user.getId() == userId) {
                return user;
            }
        }
        return null;
    }


    private void viewPostDetails(PrintWriter printWriter, BufferedReader bufferedReader, int postId,User authenticatedUser) throws IOException {
        // Find the post by its ID
        Post post = findPostById(postId);
        if (post != null) {
            printWriter.println("Post Details:");
            printWriter.println("ID: " + post.getId());
            printWriter.println("User: " + post.getUser().getName());
            printWriter.println("Content: " + post.getContent());
            printWriter.println("Likes: " + post.getLikes());
            printWriter.println("Comments: " + post.getComments().size());
            printWriter.println("Commands:");
            printWriter.println("1: Like this post");
            printWriter.println("2: Comment on this post");
            printWriter.println("3: Back to posts list");

            String userCommand = bufferedReader.readLine();
            switch (userCommand) {
                case "1":
                    likePost(post);
                    printWriter.println("Post Liked Successfully command!");
                    viewPostDetails(printWriter, bufferedReader, postId, authenticatedUser);

                    break;
                case "2":
                    commentOnPost(printWriter, bufferedReader, post,authenticatedUser);
                    viewPostDetails(printWriter, bufferedReader, postId, authenticatedUser);

                    break;
                case "3":
                    // Return to posts list
                    printPostsList(printWriter);
                    break;
                default:
                    printWriter.println("Invalid command!");
                    viewPostDetails(printWriter, bufferedReader, postId,authenticatedUser); // Stay in post details view
            }
        } else {
            printWriter.println("Post not found!");
        }
    }

    private Post findPostById(int postId) {
        // Iterate through all posts to find the post with the given ID
        for (Post post : server.getPosts()) {
            if (post.getId() == postId) {
                return post;
            }
        }
        return null;
    }

    private void likePost(Post post) {
        // Implement logic to handle liking a post
        post.addLike(); // For example, increment the likes count of the post
    }

    private void commentOnPost(PrintWriter printWriter, BufferedReader bufferedReader, Post post, User user) throws IOException {
        printWriter.println("Enter your comment:");
        String commentContent = bufferedReader.readLine();
        Comment comment = new Comment(user, commentContent);
        post.addComment(comment); // Add the comment to the post
        printWriter.println("Comment added successfully!");
    }



    private void printPostsList(PrintWriter printWriter) {
        printWriter.println("Posts List:");
        List<Post> posts = server.getPosts(); // Assuming you have a method to retrieve posts in your Server class
        if (posts.isEmpty()) {
            printWriter.println("No posts available.");
        } else {
            for (Post post : posts) {
                printWriter.println("ID: " + post.getId());
                printWriter.println("User: " + post.getUser().getName());
                printWriter.println("Content: " + post.getContent());
                printWriter.println("Likes: " + post.getLikes());
                printWriter.println("Comments: " + post.getComments().size());
                printWriter.println("---------------");
            }
        }
    }




}
