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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

public class CommentReplies extends AppCompatActivity {

    private String comment_id;
    private EditText commentField;
    private RecyclerView commentList;
    private String posterEmail;
    private FirebaseRecyclerAdapter<ForumCommentModel, CommentsViewHolder> firebaseRecyclerAdapter;

    private FirebaseAuth auth;
    private DatabaseReference comments;
    private DatabaseReference users;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_replies);

        //Get instance of auth
        auth = FirebaseAuth.getInstance();

        //Get the comment id from the comments tab
        comment_id = getIntent().getExtras().getString("comment_id");

        //Get reference to Comments database
        comments = FirebaseDatabase.getInstance().getReference().child("Comment Replies").child(comment_id);

        //Store comments to improve performance
        comments.keepSynced(true);

        commentField = findViewById(R.id.comment);
        commentList = findViewById(R.id.commentList);



        if(auth.getCurrentUser() != null) {
            user = auth.getCurrentUser();
            //Get reference to Firebase users database
            users = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        }else{
            commentField.setInputType(InputType.TYPE_NULL);

            //If the user is not logged in remove the edit text field and notify them to login
            String loggedIn = getResources().getString(R.string.logged);
            SpannableString loginLink = new SpannableString(loggedIn);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    startActivity(new Intent(CommentReplies.this, LoginActivity.class));
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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CommentReplies.this, VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        commentList.setHasFixedSize(true);
        commentList.setLayoutManager(linearLayoutManager);

        //Set title bar to display Comments
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Replies");
        }

        commentField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        commentField.setRawInputType(InputType.TYPE_CLASS_TEXT);

        //On enter execute the post method if internet is available
        commentField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (internet_connection()) {
                        post();
                    }else{
                        Toast.makeText(CommentReplies.this, "Internet Connection Required", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    //Method to check if the device has an internet connection
    boolean internet_connection(){
        ConnectivityManager connectionManager = (ConnectivityManager)CommentReplies.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectionManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

    @Override
    public void onStart(){
        super.onStart();

        //Order comments by time
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

                    String icon = "https://www.gravatar.com/avatar/" + hash +"s=2048?default=mm";

                    Glide.with(CommentReplies.this)
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
                                new AlertDialog.Builder(CommentReplies.this)
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
                                View view = CommentReplies.this.getCurrentFocus();
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
        TextView reply;
        TextView replyCount;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            comment = mView.findViewById(R.id.comment);
            commentPoster = mView.findViewById(R.id.commentPoster);
            postedByImage = mView.findViewById(R.id.postedByImage);
            singleComment = mView.findViewById(R.id.comment_single);
            reply = mView.findViewById(R.id.reply);
            replyCount = mView.findViewById(R.id.replyCount);
            replyCount.setVisibility(View.GONE);
            reply.setVisibility(View.GONE);
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
