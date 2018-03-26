package jack.village;
/**
 * Created by jack on 08/03/2018
 */

public class EventModel {

    //Model for the news feed

    private String title, content, image;

    public EventModel(){

    }



    public EventModel(String title, String content, String image) {
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
