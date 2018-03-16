package jack.village;

import java.security.PublicKey;

/**
 * Created by jack on 08/03/2018
 */

public class ForumCommentModel {

    //Model for the news forum

    private String comment, userName, uid, email;

    public ForumCommentModel() {

    }


    public ForumCommentModel(String comment, String uid, String userName, String email) {
        this.comment = comment;
        this.userName = userName;
        this.uid = uid;
        this.email = email;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}