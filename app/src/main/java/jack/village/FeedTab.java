package jack.village;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.view.View.INVISIBLE;
import static android.widget.GridLayout.VERTICAL;


/**
 * A simple {@link Fragment} subclass.
 */
public class FeedTab extends Fragment implements View.OnClickListener{

    private FloatingActionButton createPost;
    private RecyclerView feedList;
    private DatabaseReference database;
    private FirebaseAuth auth;
    private boolean isLiked = false;
    private DatabaseReference like;
    private String posterEmail;

    public FeedTab() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_feed, container, false);

        //Get reference to Feed and likes database
        database = FirebaseDatabase.getInstance().getReference().child("Feed");
        like = FirebaseDatabase.getInstance().getReference().child("Like");

        //Keep the data synced to save user data and improve load times
        database.keepSynced(true);
        like.keepSynced(true);

        createPost = view.findViewById(R.id.createPost);
        createPost.setOnClickListener(this);

        feedList = view.findViewById(R.id.feedList);

        //Set layout manager to control the flow of the feeds
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        feedList.setHasFixedSize(true);
        feedList.setLayoutManager(linearLayoutManager);

        //Get reference to auth database
        auth = FirebaseAuth.getInstance();

        return view;
    }

    public void onResume(){
        super.onResume();
        //Only allow logged in users to access the new feed button
        if (auth.getCurrentUser() == null) {
            createPost.hide();
        }
    }

    @Override
    public void onStart(){
        super.onStart();

        //Order feeds by time
        Query query = database.orderByChild("timestamp");

        FirebaseRecyclerAdapter<FeedModel, FeedViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FeedModel, FeedViewHolder>(
                FeedModel.class,
                R.layout.single_feed_layout,
                FeedViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final FeedViewHolder viewHolder, final FeedModel model, int position) {

                //Set feed_id to the current postion key
                final String feed_id = getRef(position).getKey();

                //For each feed item set all the content
                viewHolder.setPostedBy(model.getUserName());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setContent(model.getContent());
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
                viewHolder.setLike(feed_id);
                viewHolder.setLikeCount(feed_id);

                final String uid = model.getUid();

                //Set on click listener to allow users to see full article
//                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent feed = new Intent(getActivity(), FeedSingleActivity.class);
//                        feed.putExtra("feed_id", feed_id);
//                        startActivity(feed);
//                    }
//                });

                final String title = model.getTitle();
                final String content = model.getContent();
                final String imageUrl = model.getImage();

                final String share = imageUrl + "\n\n" + title + "\n\n" + content;

                //Set on click listener to display a pop up menu
                viewHolder.options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(getContext(), view);
                        MenuInflater inflater = popup.getMenuInflater();
                        if(auth.getCurrentUser() != null) {
                            if (auth.getCurrentUser().getUid().equals(uid)) {
                                inflater.inflate(R.menu.feed_menu, popup.getMenu());
                            }else{
                                inflater.inflate(R.menu.feed_menu_user, popup.getMenu());
                            }
                        }


                        popup.show();

                        //Set on click listeners to menu items
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                //Switch between different menu options
                                switch (menuItem.getItemId()) {
                                    //Allow user to delete post
                                    case R.id.delete:
                                        like.child(feed_id).removeValue();
                                        database.child(feed_id).removeValue();
                                        break;
                                    case R.id.share:
                                        Intent sendIntent = new Intent();
                                        sendIntent.setAction(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, share);
                                        sendIntent.setType("text/plain");
                                        startActivity(sendIntent);
                                        break;
                                }
                                return false;
                            }
                        });
                    }
                });

                //Set on click listener for the like button
                viewHolder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Boolean to prevent issue with live database
                        isLiked = true;

                            like.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    //If true allow user to select or deselect icon
                                    if(isLiked) {
                                            //If the user has already liked the post set remove their like
                                        if (dataSnapshot.child(feed_id).hasChild(auth.getCurrentUser().getUid())) {

                                            like.child(feed_id).child(auth.getCurrentUser().getUid()).removeValue();
                                            isLiked = false;

                                        } else {
                                            //If the user has not liked the post before then add their unique ID
                                            like.child(feed_id).child(auth.getCurrentUser().getUid()).setValue(auth.getCurrentUser().getEmail());
                                            isLiked = false;
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                });

                if(model.getEmail() != null) {
                    posterEmail = model.getEmail();
                    String hash = MD5Util.md5Hex(posterEmail);

                    String icon = "https://www.gravatar.com/avatar/" + hash +"s=2048";

                    Glide.with(FeedTab.this)
                            .load(icon)
                            .apply(new RequestOptions()
                                    .circleCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                            .into(viewHolder.postedByImage);
                }
            }
        };

        feedList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class FeedViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton like;
        ImageButton options;
        DatabaseReference likes;
        DatabaseReference likeCountDB;
        FirebaseAuth auth;
        TextView likeCount;
        Context context;
        TextView postedBy;
        ImageView postedByImage;

        public FeedViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            like = mView.findViewById(R.id.like);

            likes = FirebaseDatabase.getInstance().getReference().child("Like");
            auth = FirebaseAuth.getInstance();

            likeCount = mView.findViewById(R.id.likeCount);
            postedBy = mView.findViewById(R.id.postedBy);
            options = mView.findViewById(R.id.options);
            postedByImage = mView.findViewById(R.id.postedByImage);

            context = mView.getContext();

            likes.keepSynced(true);
        }

        public void setLike(final String feed_id) {
            likes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    //Ensure user is logged in
                    if (auth.getCurrentUser() != null) {
                        //If the users id has been added to the DB then they have liked the post - set icon colour to red
                        if (dataSnapshot.child(feed_id).hasChild(auth.getCurrentUser().getUid())) {
                            like.setColorFilter(Color.rgb(220, 20, 60));
                        } else {
                            //Else the user has unliked the post - set icon to default grey
                            like.setColorFilter(Color.rgb(211, 211, 211));
                        }
                    } else {
                        //If the user is not logged in, display dialogue box telling them they need an account
                        like.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new android.support.v7.app.AlertDialog.Builder(context)
                                        .setTitle("Sign up for Village")
                                        .setMessage("Sign up to like this post")
                                        .setCancelable(false)
                                        .setNegativeButton("Cancel", null)
                                        .setNeutralButton("Already have an account? Sign in", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(context, LoginActivity.class);
                                                context.startActivity(intent);
                                            }
                                        })
                                        .setPositiveButton("Sign Up", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(context, SignUpActivity.class);
                                                context.startActivity(intent);
                                            }
                                        })
                                        .show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setPostedBy(String userName){
            //Set text view to the user name that posted the post
            TextView postedBy = mView.findViewById(R.id.postedBy);
            postedBy.setText(userName);
        }


        public void setLikeCount(final String feed_id){
            //Get the number of children from the likes database
            likeCountDB = FirebaseDatabase.getInstance().getReference().child("Like").child(feed_id);
            likeCountDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String likeCounter = String.valueOf(dataSnapshot.getChildrenCount());
                    if(!likeCounter.isEmpty()){
                        //Set text to display the number of likes the post has
                        likeCount.setText(likeCounter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title){
            //Set title to title pulled from database
            TextView feedTitle = mView.findViewById(R.id.feedTitle);
            feedTitle.setText(title);
        }

        public void setContent(String content){
            //Set content from content pulled from database
            TextView feedContent = mView.findViewById(R.id.feedContent);
            feedContent.setText(content);
        }

        public void setImage(Context context, String image){
            //Set image from image pulled from database
            ImageView feedImage = mView.findViewById(R.id.feedImage);
            Glide.with(context)
                    .load(image)
                    .apply(new RequestOptions()
                            .override(600, 600)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .into(feedImage);


        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createPost:
                startActivity(new Intent(getActivity(), FeedActivityNew.class));
                break;
        }
    }


}
