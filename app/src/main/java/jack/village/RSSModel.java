package jack.village;

import android.support.v7.widget.CardView;

/**
 * Created by jack on 13/02/2018.
 */


//Create class to hold title, description and link
public class RSSModel {

    public String title;
    public String link;
    public String description;
    CardView podcastCard;

    public RSSModel(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
    }
}
