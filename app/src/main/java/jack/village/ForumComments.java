package jack.village;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class ForumComments extends AppCompatActivity {

    private String forum_id;
    private EditText commentField;
    private RecyclerView commentList;
    private ImageButton submit;
    private String posterEmail;
    private FirebaseRecyclerAdapter<ForumCommentModel, CommentsViewHolder> firebaseRecyclerAdapter;

    private FirebaseAuth auth;
    private DatabaseReference comments;
    private DatabaseReference forum;
    private DatabaseReference users;
    private FirebaseUser user;
    private TextView readMore;

    private ImageView forumImage;
    private TextView forumTitle;
    private TextView forumContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_comments);

        //Get instance of auth
        auth = FirebaseAuth.getInstance();

        //Get the forum id from the forum tab
        forum_id = getIntent().getExtras().getString("forum_id");

        //Get reference to Comments database
        comments = FirebaseDatabase.getInstance().getReference().child("Comment").child(forum_id);
        forum = FirebaseDatabase.getInstance().getReference().child("Forum").child(forum_id);

        //Store comments to improve performance
        comments.keepSynced(true);

        commentField = findViewById(R.id.comment);
        commentList = findViewById(R.id.commentList);
        submit = findViewById(R.id.send);

        forumTitle = findViewById(R.id.forumTitle);
        forumImage = findViewById(R.id.forumImage);
        forumContent = findViewById(R.id.forumContent);
        readMore = findViewById(R.id.readMore);

        //Add the details for forum the comments relate to
        forum.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               String title = dataSnapshot.child("title").getValue().toString();
               String content = dataSnapshot.child("content").getValue().toString();
               String image = dataSnapshot.child("image").getValue().toString();

                forumTitle.setText(title);
                forumContent.setText(content);

                Glide.with(ForumComments.this)
                        .load(image)
                        .apply(new RequestOptions()
                                .override(600, 600)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                        .into(forumImage);

                //Set max lines to four to reduce the amount of space the text view takes up
                forumContent.setMaxLines(4);

                //Allow user to expand and shrink the text view content
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(auth.getCurrentUser() != null) {
            user = auth.getCurrentUser();
            //Get reference to Firebase users database
            users = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        }else{
            submit.setVisibility(View.INVISIBLE);
            commentField.setInputType(InputType.TYPE_NULL);

            //If the user is not logged in remove the edit text field and notify them to login
            String loggedIn = getResources().getString(R.string.logged);
            SpannableString loginLink = new SpannableString(loggedIn);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    startActivity(new Intent(ForumComments.this, LoginActivity.class));
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            loginLink.setSpan(clickableSpan, 12, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            commentField.setText(loginLink);
            commentField.setMovementMethod(LinkMovementMethod.getInstance());
            commentField.setHighlightColor(getResources().getColor(R.color.colorAccent));
        }

        //Set layout manager to control the flow of the forums
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ForumComments.this, VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        commentList.setHasFixedSize(true);
        commentList.setLayoutManager(linearLayoutManager);

        //Set title bar to display Comments
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Comments");
        }

        //On submit execute the post method if internet is available
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (internet_connection()) {
                    post();
                }else{
                    Toast.makeText(ForumComments.this, "Internet Connection Required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Method to check if the device has an internet connection
    boolean internet_connection(){
        ConnectivityManager connectionManager = (ConnectivityManager)ForumComments.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectionManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

    @Override
    public void onStart(){
        super.onStart();

        //Order forums by time
        Query query = comments.orderByChild("timestamp");

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ForumCommentModel, CommentsViewHolder>(
                ForumCommentModel.class,
                R.layout.single_comment_layout,
                CommentsViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final CommentsViewHolder viewHolder, final ForumCommentModel model, int position) {

                //Get current comment id
                final String comment_id = getRef(position).getKey();

                //Set the user name and content of each comment
                viewHolder.setUserName(model.getUserName());
                viewHolder.setComment(model.getComment());

                //Set sting uid to user uid
                final String uid = model.getUid();

                //If an email exists pull image for user
                if(model.getEmail() != null) {
                    posterEmail = model.getEmail();
                    String hash = MD5Util.md5Hex(posterEmail);

                    String icon = "https://www.gravatar.com/avatar/" + hash +"s=2048";

                    Glide.with(ForumComments.this)
                            .load(icon)
                            .apply(new RequestOptions()
                                    .circleCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                            .into(viewHolder.postedByImage);
                }



                //Long on click listener to allow users to delete their own comments
                viewHolder.singleComment.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        if (auth.getCurrentUser() != null) {
                            //User must be the creator of the comment to delete it
                            if (auth.getCurrentUser().getUid().equals(uid)) {
                                new AlertDialog.Builder(ForumComments.this)
                                        .setMessage("Delete Post?")
                                        .setCancelable(false)
                                        .setNegativeButton("Cancel", null)
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                comments.child(comment_id).removeValue();
                                            }
                                        })
                                        .show();
                            }
                        }
                        return false;
                    }
                });

            }
        };

        //Set adaptor to recycler adapter and return to the top of the feed
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
                            post.child("uid").setValue(user.getUid());
                            post.child("email").setValue(user.getEmail());
                            post.child("comment").setValue(comment);
                            post.child("userName").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        commentField.setText("");
                                        commentList.scrollToPosition(firebaseRecyclerAdapter.getItemCount() -1);
                                        View view = ForumComments.this.getCurrentFocus();
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
        LinearLayout singleComment;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            comment = mView.findViewById(R.id.comment);
            commentPoster = mView.findViewById(R.id.commentPoster);
            postedByImage = mView.findViewById(R.id.postedByImage);
            singleComment = mView.findViewById(R.id.comment_single);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }



}
