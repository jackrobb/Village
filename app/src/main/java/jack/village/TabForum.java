package jack.village;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import static android.widget.GridLayout.VERTICAL;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabForum extends Fragment implements View.OnClickListener{

    private FloatingActionButton createPost;
    private RecyclerView forumList;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference database;

    public TabForum() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_forum , container, false);

        database = FirebaseDatabase.getInstance().getReference().child("Forum");

        createPost = view.findViewById(R.id.createPost);
        createPost.setOnClickListener(this);

        forumList = view.findViewById(R.id.forumList);

        linearLayoutManager = new LinearLayoutManager(getActivity(), VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        forumList.setHasFixedSize(true);
        forumList.setLayoutManager(linearLayoutManager);

        database.keepSynced(true);

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();

        FirebaseRecyclerAdapter<ForumModel, ForumViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ForumModel, ForumViewHolder>(
                ForumModel.class,
                R.layout.single_forum_layout,
                ForumViewHolder.class,
                database
        ) {
            @Override
            protected void populateViewHolder(ForumViewHolder viewHolder, ForumModel model, int position) {
                viewHolder.setTitle(model.getTitle());
                viewHolder.setContent(model.getContent());
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
            }
        };

        forumList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class ForumViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ForumViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
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
            Picasso.with(context).load(image).into(forumImage);
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
