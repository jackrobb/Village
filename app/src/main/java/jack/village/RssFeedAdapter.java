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
    String podcast1;
    String title;


    public static class viewHolder extends RecyclerView.ViewHolder {
        private View rssFeedView;
        public CardView cardView;

        public viewHolder(View v) {
            super(v);
            rssFeedView = v;
            cardView = itemView.findViewById(R.id.podcastCard);
        }
    }

    public RssFeedAdapter(List<RSSModel> RSSModels) {
        rssModels = RSSModels;
    }

    //Inflate the view used for each list item
    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_podcast_layout, parent, false);
        viewHolder feedHolder = new viewHolder(v);
        return feedHolder;


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

                Uri podcastUri = Uri.parse(RSSModel.link);
                podcast1 = podcastUri.toString();
                Uri titleUri = Uri.parse(RSSModel.title);
                title = titleUri.toString();

                Context context = view.getContext();
                Intent intent = new Intent(context, PodcastPlayer.class);
                intent.putExtra("title", title);
                intent.putExtra("url", podcast1);
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


