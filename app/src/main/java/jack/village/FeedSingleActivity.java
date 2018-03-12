package jack.village;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FeedSingleActivity extends AppCompatActivity {

    private DatabaseReference database;
    private DatabaseReference like;

    private String feed_id;
    private String uid;

    private TextView title;
    private TextView content;
    private ImageView image;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_single);

        //Get reference to feed and like database
        database = FirebaseDatabase.getInstance().getReference().child("Feed");
        like = FirebaseDatabase.getInstance().getReference().child("Like");

        //Get instance of auth
        auth = FirebaseAuth.getInstance();

        //Get the feed id from the feed tab
        feed_id = getIntent().getExtras().getString("feed_id");

        title = findViewById(R.id.feedTitle);
        content = findViewById(R.id.feedContent);
        image = findViewById(R.id.feedImage);

        Toolbar toolbar = findViewById(R.id.feedToolBar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //Set listener for child of the feed_id pulled from the previous activity
        database.child(feed_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Pull the title, content and image
                String feedTitle = (String) dataSnapshot.child("title").getValue();
                String feedContent = (String) dataSnapshot.child("content").getValue();
                String feedImage = (String) dataSnapshot.child("image").getValue();
                uid = (String) dataSnapshot.child("uid").getValue();

                //Set them to displayed feed
                title.setText(feedTitle);
                content.setText(feedContent);

                Glide.with(getApplicationContext())
                        .load(feedImage)
                        .apply(new RequestOptions()
                                .override(600, 600)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                        .into(image);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //If the user who posted the post is signed in allow them to delete it
        if(auth.getCurrentUser() != null) {
            if (auth.getCurrentUser().getUid().equals(uid)) {
                getMenuInflater().inflate(R.menu.note_menu, menu);
            }
        }

        return true;
    }

    private void deleteFeed(){
        //Delete feed and likes from the database
        like.child(feed_id).removeValue();
        database.child(feed_id).removeValue();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case R.id.noteDelete:
                    deleteFeed();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

}
