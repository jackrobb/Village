package jack.village;


/**
 * Created by jack on 13/02/2018.
 */


//Create class to hold title, description and link
public class PodcastRSSModel {

    public String title;
    public String link;
    public String description;

    public PodcastRSSModel(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
    }
}
