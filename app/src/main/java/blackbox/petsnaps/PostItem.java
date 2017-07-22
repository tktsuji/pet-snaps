package blackbox.petsnaps;

/**
 * Created by tricia on 7/18/17.
 */

public class PostItem {

    private String title;
    private String descrp;
    private String image;
    private String uid;
    private long reverse_timestamp;
    private String username;

    public PostItem() {

    }

    public PostItem(String descrp, String image, String title, String username, String uid, long reverse_timestamp) {
        this.descrp = descrp;
        this.image = image;
        this.title = title;
        this.username = username;
        this.reverse_timestamp = reverse_timestamp;
        this.uid = uid;
    }

    public String getDescrp() {
        return descrp;
    }

    public void setDescrp(String descrp) {
        this.descrp = descrp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getReverse_timestamp() {
        return reverse_timestamp;
    }

    public void setReverse_timestamp(long reverse_timestamp) {
        this.reverse_timestamp = reverse_timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
