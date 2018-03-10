package jack.village;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
public class TabForum extends Fragment implements View.OnClickListener{

    private FloatingActionButton createPost;
    private RecyclerView forumList;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference database;
    private FirebaseAuth auth;
    private boolean isLiked = false;
    private DatabaseReference like;

    public TabForum() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_forum , container, false);

        database = FirebaseDatabase.getInstance().getReference().child("Forum");
        like = FirebaseDatabase.getInstance().getReference().child("Like");

        database.keepSynced(true);
        like.keepSynced(true);

        createPost = view.findViewById(R.id.createPost);
        createPost.setOnClickListener(this);

        forumList = view.findViewById(R.id.forumList);

        linearLayoutManager = new LinearLayoutManager(getActivity(), VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        forumList.setHasFixedSize(true);
        forumList.setLayoutManager(linearLayoutManager);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            database.keepSynced(true);
        }

        return view;
    }

    public void onResume(){
        super.onResume();
        if (auth.getCurrentUser() == null) {
            createPost.hide();
        }
    }

    @Override
    public void onStart(){
        super.onStart();

        Query query = database.orderByChild("timestamp").limitToLast(15);

        FirebaseRecyclerAdapter<ForumModel, ForumViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ForumModel, ForumViewHolder>(
                ForumModel.class,
                R.layout.single_forum_layout,
                ForumViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(ForumViewHolder viewHolder, ForumModel model, int position) {

                final String forum_id = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setContent(model.getContent());
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
                viewHolder.setLike(forum_id);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent forum = new Intent(getActivity(), ForumSingleActivity.class);
                        forum.putExtra("forum_id", forum_id);
                        startActivity(forum);
                    }
                });

                viewHolder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isLiked = true;

                            like.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(isLiked) {

                                        if (dataSnapshot.child(forum_id).hasChild(auth.getCurrentUser().getUid())) {

                                            like.child(forum_id).child(auth.getCurrentUser().getUid()).removeValue();
                                            isLiked = false;

                                        } else {
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

            }
        };

        forumList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class ForumViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton like;
        DatabaseReference likes;
        FirebaseAuth auth;

        public ForumViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            like = mView.findViewById(R.id.like);

            likes = FirebaseDatabase.getInstance().getReference().child("Like");
            auth = FirebaseAuth.getInstance();

            likes.keepSynced(true);
        }

        public void setLike(final String forum_id){
            likes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(auth.getCurrentUser() != null) {
                        if (dataSnapshot.child(forum_id).hasChild(auth.getCurrentUser().getUid())) {
                            like.setColorFilter(Color.rgb(220, 20, 60));
                        } else {
                            like.setColorFilter(Color.rgb(211, 211, 211));
                        }
                    }else{
                        like.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title){
            TextView forumTitle = mView.findViewById(R.id.forumTitle);
            forumTitle.setText(title);
        }

        public void setContent(String content){
            TextView forumContent = mView.findViewById(R.id.forumContent);
            forumContent.setText(content);
        }

        public void setImage(Context context, String image){
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
                startActivity(new Intent(getActivity(), NewForum.class));
                break;
        }
    }

}
