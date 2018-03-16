package jack.village;
/**
 * Created by jack on 08/03/2018
 */

public class ForumModel {

    //Model for the news feed

    private String title, content, image, userName, uid, email;

    public ForumModel(){

    }



    public ForumModel(String title, String content, String image, String uid, String userName, String email) {
        this.title = title;
        this.userName = userName;
        this.content = content;
        this.image = image;
        this.uid = uid;
        this.email = email;

    }

    public String getEmail(){
        return email;
    }

    public String getUid(){return uid;}

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
