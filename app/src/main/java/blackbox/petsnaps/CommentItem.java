package blackbox.petsnaps;

/**
 * Created by tricia on 7/26/17.
 */

public class CommentItem {
    private String username;
    private String uid;
    private String message;
    private String postKey;

    public CommentItem() {}

    public CommentItem(String username, String uid, String message, String postKey) {
        this.username = username;
        this.uid = uid;
        this.message = message;
        this.postKey = postKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }





}
