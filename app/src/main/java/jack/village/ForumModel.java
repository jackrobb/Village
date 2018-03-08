package jack.village;

/**
 * Created by jack on 08/03/2018.
 */

public class ForumModel {

    private String title, content, image, uid;

    public ForumModel(){

    }

    public ForumModel(String title, String content, String image, String uid) {
        this.title = title;
        this.content = content;
        this.image = image;
        this.uid = uid;
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

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
