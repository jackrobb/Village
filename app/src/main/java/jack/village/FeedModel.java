package jack.village;

/**
 * Created by jack on 08/03/2018
 */

public class FeedModel {

    //Model for the news feed

    private String title, content, image, userName;

    public FeedModel(){

    }



    public FeedModel(String title, String content, String image, String uid, String userName) {
        this.title = title;
        this.userName = userName;
        this.content = content;
        this.image = image;

    }

    public String getUserName() {
        return userName;
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
