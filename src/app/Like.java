package app;

public class Like {
    private int posId;
    private User user;

    public Like(User user,int postId) {
        this.user = user;
        this.posId=postId;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
