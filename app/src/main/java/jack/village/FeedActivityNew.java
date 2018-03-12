package jack.village;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FeedActivityNew extends AppCompatActivity {

    private ImageButton feedImage;
    private EditText feedTitle;
    private EditText feedContent;
    private ProgressDialog progressDialog;

    private FirebaseUser user;
    private DatabaseReference users;

    private Uri image = null;

    private StorageReference storage;
    private DatabaseReference database;

    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_feed);

        //Hide title from action bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //Show back button on action bar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Get reference to fFrebase database and storage for feeds
        database = FirebaseDatabase.getInstance().getReference().child("Feed");
        storage = FirebaseStorage.getInstance().getReference();

        //Get instance of auth and user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        //Get reference to Firebase users database
        users = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        feedImage = findViewById(R.id.feedImage);
        feedTitle = findViewById(R.id.feedTitle);
        feedContent = findViewById(R.id.feedContent);
        Button submit = findViewById(R.id.feedSubmit);

        progressDialog = new ProgressDialog(FeedActivityNew.this);
        progressDialog.setMessage("Uploading...");

        //On submit click show dialogue box and post new feed article
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                post();
            }
        });

        //Allow user to upload an image from their gallery
        feedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageGallery = new Intent(Intent.ACTION_GET_CONTENT);
                imageGallery.setType("image/*");
                startActivityForResult(imageGallery, GALLERY_REQUEST);
            }
        });


    }

    private void post(){
        //Get the user inputs from the text fields
        final String title = feedTitle.getText().toString().trim();
        final String content = feedContent.getText().toString().trim();

        //If they're not empty upload them to the database
        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content) && image!= null) {
            long time = System.currentTimeMillis();
            String unique = String.valueOf(time);
            StorageReference file = storage.child("Feed_Images").child(unique); //Set image to unique ID of timestamp
            file.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //Get download url for image and post data to the database
                    final Uri download = taskSnapshot.getDownloadUrl();
                    final  DatabaseReference post = database.push();

                    users.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Post each item to the database
                            post.child("title").setValue(title);
                            post.child("content").setValue(content);
                            post.child("image").setValue(download.toString());
                            post.child("uid").setValue(user.getUid());
                            post.child("email").setValue(user.getEmail());
                            post.child("userName").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        finish();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    progressDialog.dismiss();


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
            feedImage.setImageURI(image);
        }
    }

}
