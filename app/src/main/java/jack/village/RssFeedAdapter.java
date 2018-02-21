package jack.village;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jack on 13/02/2018
 */

public class RssFeedAdapter extends RecyclerView.Adapter<RssFeedAdapter.viewHolder>{

    private List<RSSModel> rssModels;
    private String podcast1;
    private String title;
    private String description;


    static class viewHolder extends RecyclerView.ViewHolder {
        private View rssFeedView;
        CardView cardView;

        viewHolder(View v) {
            super(v);
            rssFeedView = v;
            cardView = itemView.findViewById(R.id.podcastCard);
        }
    }

    RssFeedAdapter(List<RSSModel> RSSModels) {
        rssModels = RSSModels;
    }

    //Inflate the view used for each list item
    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_podcast_layout, parent, false);
        return new viewHolder(v);


    }

    //Bind the values from the objects to the view
    @Override
    public void onBindViewHolder(viewHolder holder, int position) {
        final RSSModel RSSModel = rssModels.get(position);
        ((TextView)holder.rssFeedView.findViewById(R.id.titleText)).setText(RSSModel.title);
        ((TextView)holder.rssFeedView.findViewById(R.id.descriptionText)).setText(RSSModel.description);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Get the Link set it to a string
                Uri podcastUri = Uri.parse(RSSModel.link);
                podcast1 = podcastUri.toString();

                //Get the title and set it to a string
                Uri titleUri = Uri.parse(RSSModel.title);
                title = titleUri.toString();

                //Get the description and set it to a string
                Uri descriptionUri = Uri.parse(RSSModel.description);
                description = descriptionUri.toString();

                //New intent to start podcast player, passing the data through with the intent
                Context context = view.getContext();
                Intent intent = new Intent(context, PodcastPlayer.class);
                intent.putExtra("title", title);
                intent.putExtra("url", podcast1);
                intent.putExtra("description", description);
                context.startActivity(intent);
            }
        });

    }

    //Returns the number of items in the list
    @Override
    public int getItemCount() {
        return rssModels.size();
    }

}


