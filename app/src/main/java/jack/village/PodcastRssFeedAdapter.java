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

public class PodcastRssFeedAdapter extends RecyclerView.Adapter<PodcastRssFeedAdapter.viewHolder>{

    private List<PodcastRSSModel> podcastRssModels;
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

    PodcastRssFeedAdapter(List<PodcastRSSModel> PodcastRSSModels) {
        podcastRssModels = PodcastRSSModels;
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
        final PodcastRSSModel PodcastRSSModel = podcastRssModels.get(position);
        ((TextView)holder.rssFeedView.findViewById(R.id.titleText)).setText(PodcastRSSModel.title);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the Link set it to a string
                Uri podcastUri = Uri.parse(PodcastRSSModel.link);
                podcast1 = podcastUri.toString();

                //Get the title and set it to a string
                Uri titleUri = Uri.parse(PodcastRSSModel.title);
                title = titleUri.toString();

                //Get the description and set it to a string
                Uri descriptionUri = Uri.parse(PodcastRSSModel.description);
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
        return podcastRssModels.size();
    }

}


