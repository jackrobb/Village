package jack.village;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

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

public class ForumActivityNew extends AppCompatActivity {

    private ImageButton forumImage;
    private EditText forumTitle;
    private EditText forumContent;
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
        setContentView(R.layout.activity_new_forum);

        //Hide title from action bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //Show back button on action bar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Get reference to firebase database and storage for forums
        database = FirebaseDatabase.getInstance().getReference().child("Forum");
        storage = FirebaseStorage.getInstance().getReference();

        //Get instance of auth and user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        //Get reference to Firebase users database
        users = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        forumImage = findViewById(R.id.forumImage);
        forumTitle = findViewById(R.id.forumTitle);
        forumContent = findViewById(R.id.forumContent);
        Button submit = findViewById(R.id.forumSubmit);

        progressDialog = new ProgressDialog(ForumActivityNew.this);
        progressDialog.setMessage("Uploading...");

        //On submit click show dialogue box and post new forum article
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (internet_connection()) {
                    post();
                }else{
                    Toast.makeText(ForumActivityNew.this, "Internet Connection Required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Allow user to upload an image from their gallery
        forumImage.setOnClickListener(new View.OnClickListener() {
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
        final String title = forumTitle.getText().toString().trim();
        final String content = forumContent.getText().toString().trim();

        //If there is no title break and set focus
        if(title.isEmpty()){
            forumTitle.setError("Title Required");
            forumTitle.requestFocus();
            return;
        }

        //If there is no image break and set focus
        if(image == null){
            forumTitle.setError("Image Required");
            forumImage.requestFocus();
            return;
        }

        progressDialog.show();

        //If they're not empty upload them to the database
        if(!TextUtils.isEmpty(title) && image!= null) {
            long time = System.currentTimeMillis();
            String unique = String.valueOf(time);
            StorageReference file = storage.child("Forum_Images").child(unique); //Set image to unique ID of timestamp
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

                            //Set lowercase title for search functionality
                            post.child("titleLowerCase").setValue(title.toLowerCase());
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
            forumImage.setImageURI(image);
        }
    }

    //Method to check if the device has an internet connection
    boolean internet_connection(){
        ConnectivityManager connectionManager = (ConnectivityManager)ForumActivityNew.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectionManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;

    }

}
