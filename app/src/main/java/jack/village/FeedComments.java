package jack.village;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.widget.GridLayout.VERTICAL;

public class FeedComments extends AppCompatActivity {

    private String feed_id;
    private EditText commentField;
    private RecyclerView commentList;
    private ImageButton submit;
    private String posterEmail;
    private FirebaseRecyclerAdapter<FeedCommentModel, CommentsViewHolder> firebaseRecyclerAdapter;

    private FirebaseAuth auth;
    private DatabaseReference comments;
    private DatabaseReference users;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_comments);

        //Get instance of auth
        auth = FirebaseAuth.getInstance();

        //Get the feed id from the feed tab
        feed_id = getIntent().getExtras().getString("feed_id");

        //Get reference to Comments database
        comments = FirebaseDatabase.getInstance().getReference().child("Comment").child(feed_id);

        comments.keepSynced(true);

        commentField = findViewById(R.id.comment);
        commentList = findViewById(R.id.commentList);
        submit = findViewById(R.id.send);

        user = auth.getCurrentUser();
        //Get reference to Firebase users database
        users = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        //Set layout manager to control the flow of the feeds
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FeedComments.this, VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);

        commentList.setHasFixedSize(true);
        commentList.setLayoutManager(linearLayoutManager);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Comments");
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (internet_connection()) {
                    post();
                }else{
                    Toast.makeText(FeedComments.this, "Internet Connection Required", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //Method to check if the device has an internet connection
    boolean internet_connection(){
        ConnectivityManager connectionManager = (ConnectivityManager)FeedComments.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectionManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

    @Override
    public void onStart(){
        super.onStart();

        //Order feeds by time
        Query query = comments.orderByChild("timestamp");

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FeedCommentModel, CommentsViewHolder>(
                FeedCommentModel.class,
                R.layout.single_comment_layout,
                CommentsViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final CommentsViewHolder viewHolder, final FeedCommentModel model, int position) {

                    viewHolder.setUserName(model.getUserName());
                    viewHolder.setComment(model.getComment());

                if(model.getEmail() != null) {
                    posterEmail = model.getEmail();
                    String hash = MD5Util.md5Hex(posterEmail);

                    String icon = "https://www.gravatar.com/avatar/" + hash +"s=2048";

                    Glide.with(FeedComments.this)
                            .load(icon)
                            .apply(new RequestOptions()
                                    .circleCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                            .into(viewHolder.postedByImage);
                }
            }
        };

        commentList.setAdapter(firebaseRecyclerAdapter);
        commentList.scrollToPosition(firebaseRecyclerAdapter.getItemCount() -1);

    }

    private void post(){
        //Get the user inputs from the comment field
        final String comment = commentField.getText().toString().trim();

        //If there is no comment just return nothing
        if(comment.isEmpty()){
            return;
        }

        //If they're not empty upload them to the database
        if(!TextUtils.isEmpty(comment)) {

            final  DatabaseReference post = comments.push();

                    users.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Post each item to the database
                            post.child("email").setValue(user.getEmail());
                            post.child("comment").setValue(comment);
                            post.child("userName").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        commentField.setText("");
                                        commentList.scrollToPosition(firebaseRecyclerAdapter.getItemCount() -1);
                                        View view = FeedComments.this.getCurrentFocus();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }


    public static class CommentsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView comment;
        TextView commentPoster;
        ImageView postedByImage;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            comment = mView.findViewById(R.id.comment);
            commentPoster = mView.findViewById(R.id.commentPoster);
            postedByImage = mView.findViewById(R.id.postedByImage);
        }

        public void setUserName(String userName){
            //Set title to title pulled from database
            TextView user = mView.findViewById(R.id.commentPoster);
            user.setText(userName);
        }

        public void setComment(String comment){
            //Set content from content pulled from database
            TextView commentBox = mView.findViewById(R.id.comment);
            commentBox.setText(comment);
        }

    }



}
