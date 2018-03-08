package jack.village;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NewForum extends AppCompatActivity {

    private ImageButton forumImage;
    private EditText forumTitle;
    private EditText forumContent;
    private Button submit;
    private ProgressBar progress;

    private Uri image = null;

    private StorageReference storage;
    private DatabaseReference database;

    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_forum);

        storage = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference().child("Forum");

        forumImage = findViewById(R.id.forumImage);
        forumTitle = findViewById(R.id.forumTitle);
        forumContent = findViewById(R.id.forumContent);
        submit = findViewById(R.id.forumSubmit);

        progress = findViewById(R.id.forumProgress);
        progress.setVisibility(View.INVISIBLE);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
    }

    private void post(){
        final String title = forumTitle.getText().toString().trim();
        final String content = forumContent.getText().toString().trim();

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content) && image!= null){
            progress.setVisibility(View.VISIBLE);
            long time= System.currentTimeMillis();
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

                    progress.setVisibility(View.GONE);

                    finish();
                }
            });
        }
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
