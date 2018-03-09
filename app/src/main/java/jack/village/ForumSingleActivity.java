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

public class ForumSingleActivity extends AppCompatActivity {

    private DatabaseReference database;

    private String forum_id;
    private String uid;

    private TextView title;
    private TextView content;
    private ImageView image;
    private Toolbar toolbar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_single);

        database = FirebaseDatabase.getInstance().getReference().child("Forum");

        auth = FirebaseAuth.getInstance();

        forum_id = getIntent().getExtras().getString("forum_id");

        title = findViewById(R.id.forumTitle);
        content = findViewById(R.id.forumContent);
        image = findViewById(R.id.forumImage);

        toolbar = findViewById(R.id.forumToolBar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        database.child(forum_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String forumTitle = (String) dataSnapshot.child("title").getValue();
                String forumContent = (String) dataSnapshot.child("content").getValue();
                String forumImage = (String) dataSnapshot.child("image").getValue();
                uid = (String) dataSnapshot.child("uid").getValue();

                title.setText(forumTitle);
                content.setText(forumContent);

                Glide.with(getApplicationContext())
                        .load(forumImage)
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

        if(auth.getCurrentUser() != null) {
            if (auth.getCurrentUser().getUid().equals(uid)) {
                getMenuInflater().inflate(R.menu.note_menu, menu);
            }
        }

        return true;
    }

    private void deleteForum(){
        database.child(forum_id).removeValue();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case R.id.noteDelete:
                    deleteForum();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

}
