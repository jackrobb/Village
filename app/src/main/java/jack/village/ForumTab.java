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
import android.widget.EditText;
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

import static android.widget.GridLayout.VERTICAL;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForumTab extends Fragment implements View.OnClickListener{

    private FloatingActionButton createPost;
    private RecyclerView forumList;
    private DatabaseReference database;
    private FirebaseAuth auth;
    private boolean isLiked = false;
    private DatabaseReference like;
    private String posterEmail;
    private EditText search;
    private ImageButton submit;

    public ForumTab() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_forum, container, false);

        //Get reference to forum and likes database
        database = FirebaseDatabase.getInstance().getReference().child("Forum");
        like = FirebaseDatabase.getInstance().getReference().child("Like");

        //Keep the data synced to save user data and improve load times
        database.keepSynced(true);
        like.keepSynced(true);

        search = view.findViewById(R.id.search);
        submit = view.findViewById(R.id.searchBtn);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchResult = search.getText().toString();
                searchQuery(searchResult);
                search.setText("");
            }
        });

        createPost = view.findViewById(R.id.createPost);
        createPost.setOnClickListener(this);

        forumList = view.findViewById(R.id.forumList);

        //Set layout manager to control the flow of the forums
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        forumList.setHasFixedSize(true);
        forumList.setLayoutManager(linearLayoutManager);

        //Get reference to auth database
        auth = FirebaseAuth.getInstance();

        return view;
    }

    public void onResume(){
        super.onResume();
        //Only allow logged in users to access the new forum button
        if (auth.getCurrentUser() == null) {
            createPost.hide();
        }
    }

    @Override
    public void onStart(){
        super.onStart();

        //Order forums by time
        Query query = database.orderByChild("timestamp");

        FirebaseRecyclerAdapter<ForumModel, ForumViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ForumModel, ForumViewHolder>(
                ForumModel.class,
                R.layout.single_forum_layout,
                ForumViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final ForumViewHolder viewHolder, final ForumModel model, int position) {

                //Set forum_id to the current postion key
                final String forum_id = getRef(position).getKey();

                //For each forum item set all the content
                viewHolder.setPostedBy(model.getUserName());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setContent(model.getContent());
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
                viewHolder.setLike(forum_id);
                viewHolder.setLikeCount(forum_id);
                viewHolder.setCommentCount(forum_id);

                final String uid = model.getUid();

                //Set on click listener to allow users to see full article
                viewHolder.comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent forum = new Intent(getActivity(), ForumComments.class);
                        forum.putExtra("forum_id", forum_id);
                        startActivity(forum);
                    }
                });

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
                                inflater.inflate(R.menu.forum_menu, popup.getMenu());
                            }else{
                                inflater.inflate(R.menu.forum_menu_user, popup.getMenu());
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
                                        like.child(forum_id).removeValue();
                                        database.child(forum_id).removeValue();
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
                                        if (dataSnapshot.child(forum_id).hasChild(auth.getCurrentUser().getUid())) {

                                            like.child(forum_id).child(auth.getCurrentUser().getUid()).removeValue();
                                            isLiked = false;

                                        } else {
                                            //If the user has not liked the post before then add their unique ID
                                            like.child(forum_id).child(auth.getCurrentUser().getUid()).setValue(auth.getCurrentUser().getEmail());
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

                    Glide.with(ForumTab.this)
                            .load(icon)
                            .apply(new RequestOptions()
                                    .circleCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                            .into(viewHolder.postedByImage);
                }
            }
        };

        forumList.setAdapter(firebaseRecyclerAdapter);

    }

    //Allow users to search forum for topics
    public void searchQuery(String searchResult){
        //Order forums by user search term
        Query searchQuery = database.orderByChild("title").startAt(searchResult).endAt(searchResult + "\uf8ff");

        FirebaseRecyclerAdapter<ForumModel, ForumViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ForumModel, ForumViewHolder>(
                ForumModel.class,
                R.layout.single_forum_layout,
                ForumViewHolder.class,
                searchQuery
        ) {
            @Override
            protected void populateViewHolder(final ForumViewHolder viewHolder, final ForumModel model, int position) {

                //Set forum_id to the current postion key
                final String forum_id = getRef(position).getKey();

                //For each forum item set all the content
                viewHolder.setPostedBy(model.getUserName());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setContent(model.getContent());
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
                viewHolder.setLike(forum_id);
                viewHolder.setLikeCount(forum_id);
                viewHolder.setCommentCount(forum_id);

                final String uid = model.getUid();

                //Set on click listener to allow users to see full article
                viewHolder.comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent forum = new Intent(getActivity(), ForumComments.class);
                        forum.putExtra("forum_id", forum_id);
                        startActivity(forum);
                    }
                });

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
                                inflater.inflate(R.menu.forum_menu, popup.getMenu());
                            }else{
                                inflater.inflate(R.menu.forum_menu_user, popup.getMenu());
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
                                        like.child(forum_id).removeValue();
                                        database.child(forum_id).removeValue();
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
                                    if (dataSnapshot.child(forum_id).hasChild(auth.getCurrentUser().getUid())) {

                                        like.child(forum_id).child(auth.getCurrentUser().getUid()).removeValue();
                                        isLiked = false;

                                    } else {
                                        //If the user has not liked the post before then add their unique ID
                                        like.child(forum_id).child(auth.getCurrentUser().getUid()).setValue(auth.getCurrentUser().getEmail());
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

                    Glide.with(ForumTab.this)
                            .load(icon)
                            .apply(new RequestOptions()
                                    .circleCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                            .into(viewHolder.postedByImage);
                }
            }
        };

        forumList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class ForumViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton like;
        ImageButton comments;
        ImageButton options;
        DatabaseReference likes;
        DatabaseReference likeCountDB;
        DatabaseReference commentCountDB;
        FirebaseAuth auth;
        TextView likeCount;
        TextView commentCount;
        Context context;
        TextView postedBy;
        ImageView postedByImage;
        TextView readMore;

        public ForumViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            like = mView.findViewById(R.id.like);

            likes = FirebaseDatabase.getInstance().getReference().child("Like");
            auth = FirebaseAuth.getInstance();

            likeCount = mView.findViewById(R.id.likeCount);
            postedBy = mView.findViewById(R.id.postedBy);
            options = mView.findViewById(R.id.options);
            postedByImage = mView.findViewById(R.id.postedByImage);
            comments = mView.findViewById(R.id.comment);
            commentCount = mView.findViewById(R.id.commentCount);
            readMore = mView.findViewById(R.id.readMore);

            context = mView.getContext();

            likes.keepSynced(true);
        }

        public void setLike(final String forum_id) {
            likes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    //Ensure user is logged in
                    if (auth.getCurrentUser() != null) {
                        //If the users id has been added to the DB then they have liked the post - set icon colour to red
                        if (dataSnapshot.child(forum_id).hasChild(auth.getCurrentUser().getUid())) {
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


        public void setLikeCount(final String forum_id){
            //Get the number of children from the likes database
            likeCountDB = FirebaseDatabase.getInstance().getReference().child("Like").child(forum_id);
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

        public void setCommentCount(final String forum_id){
            //Get the number of children from the likes database
            commentCountDB = FirebaseDatabase.getInstance().getReference().child("Comment").child(forum_id);
            commentCountDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String commentCounter = String.valueOf(dataSnapshot.getChildrenCount());
                    if(!commentCounter.isEmpty()){
                        //Set text to display the number of likes the post has
                        commentCount.setText(commentCounter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title){
            //Set title to title pulled from database
            TextView forumTitle = mView.findViewById(R.id.forumTitle);
            forumTitle.setText(title);
        }

        public void setContent(String content){
            //Set content from content pulled from database
            final TextView forumContent = mView.findViewById(R.id.forumContent);
            forumContent.setText(content);

            //Set max lines to 4 and allow users to expand and shrink the text view
            forumContent.setMaxLines(4);
            forumContent.post(new Runnable() {
                @Override
                public void run() {
                    int lines = forumContent.getLineCount();
                    if(lines > 4) {
                        forumContent.setMaxLines(4);
                        readMore.setVisibility(View.VISIBLE);

                        readMore.setOnClickListener(new View.OnClickListener() {
                            Boolean full = false;
                            @Override
                            public void onClick(View view) {

                                if(!full) {
                                    readMore.setText(R.string.less);
                                    forumContent.setMaxLines(Integer.MAX_VALUE);
                                    full = true;
                                }else if(full){
                                    readMore.setText(R.string.more);
                                    forumContent.setMaxLines(4);
                                    full = false;
                                }
                            }
                        });

                    }
                }
            });
        }

        public void setImage(Context context, String image){
            //Set image from image pulled from database
            ImageView forumImage = mView.findViewById(R.id.forumImage);
            Glide.with(context)
                    .load(image)
                    .apply(new RequestOptions()
                            .override(600, 600)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .into(forumImage);


        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createPost:
                startActivity(new Intent(getActivity(), ForumActivityNew.class));
                break;
        }
    }


}
