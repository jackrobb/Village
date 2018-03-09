package jack.village;

/**
 * Created by jack on 08/03/2018
 */

public class ForumModel {

    private String title, content, image, email;

    public ForumModel(){

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ForumModel(String title, String content, String image, String uid, String email) {
        this.title = title;
        this.content = content;
        this.image = image;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }
}
