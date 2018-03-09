package jack.village;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NewForum extends AppCompatActivity {

    private ImageButton forumImage;
    private EditText forumTitle;
    private EditText forumContent;
    private Button submit;
    private ProgressDialog progressDialog;

    private Uri image = null;

    private StorageReference storage;
    private DatabaseReference database;

    private String forumID;
    private boolean exists;

    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_forum);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        try {
            forumID = getIntent().getStringExtra("forumId");

            //Set boolean to true is note exists
            exists = !forumID.trim().isEmpty();

        } catch (Exception e) {
            e.printStackTrace();
        }

        database = FirebaseDatabase.getInstance().getReference().child("Forum");
        storage = FirebaseStorage.getInstance().getReference();


        forumImage = findViewById(R.id.forumImage);
        forumTitle = findViewById(R.id.forumTitle);
        forumContent = findViewById(R.id.forumContent);
        submit = findViewById(R.id.forumSubmit);

        progressDialog = new ProgressDialog(NewForum.this);
        progressDialog.setMessage("Uploading...");


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                post();
            }
        });

        forumImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageGallery = new Intent(Intent.ACTION_GET_CONTENT);
                imageGallery.setType("image/*");
                startActivityForResult(imageGallery, GALLERY_REQUEST);
            }
        });

        putData();
    }

    private void putData() {
        //Fills in existing data from the users note
        if (exists) {
            database.child(forumID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")) {
                        String title = dataSnapshot.child("title").getValue().toString();
                        String content = dataSnapshot.child("content").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();

                        forumTitle.setText(title);
                        forumContent.setText(content);
                        Glide.with(getApplicationContext())
                                .load(image)
                                .apply(new RequestOptions()
                                        .override(600, 600)
                                        .centerCrop()
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                                .into(forumImage);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void post(){
        final String title = forumTitle.getText().toString().trim();
        final String content = forumContent.getText().toString().trim();

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content) && image!= null) {
            long time = System.currentTimeMillis();
            String unique = String.valueOf(time);
            StorageReference file = storage.child("Forum_Images").child(unique);
            file.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri download = taskSnapshot.getDownloadUrl();

                    DatabaseReference post = database.push();

                    post.child("title").setValue(title);
                    post.child("content").setValue(content);
                    post.child("image").setValue(download.toString());
                    post.child("uid").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    post.child("email").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                    progressDialog.dismiss();

                    finish();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        //Switch between different menu options
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            image = data.getData();
            forumImage.setImageURI(image);
        }
    }

}
