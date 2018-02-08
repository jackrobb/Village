package jack.village;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class NewNoteActivity extends AppCompatActivity {

    private Button newNoteButton;
    private EditText newNoteTitle;
    private EditText newNoteContent;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference notesDatabase;

    private String noteID;

    private boolean exists;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //Initiate Note Menu which contains button to delete note
        getMenuInflater().inflate(R.menu.note_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        //Check to see if the note exists
        try {
            noteID = getIntent().getStringExtra("noteId");

            //Set boolean to true is note exists
            if (!noteID.trim().equals("")) {
                exists = true;
            } else {
                exists = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        newNoteButton = findViewById(R.id.newNoteButton);
        newNoteTitle = findViewById(R.id.newNoteTitle);
        newNoteContent = findViewById(R.id.newNoteContent);
        toolbar = findViewById(R.id.newNoteToolBar);

        //Set toolbar to custom toolbar
        setSupportActionBar(toolbar);

        //Obtain an instance of the FirebaseAuth class
        mAuth = FirebaseAuth.getInstance();

        //Gets reference to the current users notes location of the database
        notesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid());


        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = newNoteTitle.getText().toString().trim();
                String content = newNoteContent.getText().toString().trim();

                //If both fields are filled it will call method createNote
                if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)){
                    createNote(title, content);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Complete All Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Calls method put data
        putData();
    }

    private void putData() {
        //Fills in existing data from the users note
        if (exists) {
            notesDatabase.child(noteID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")) {
                        String title = dataSnapshot.child("title").getValue().toString();
                        String content = dataSnapshot.child("content").getValue().toString();

                        newNoteTitle.setText(title);
                        newNoteContent.setText(content);

                        //Button value changed from create note to update note
                        newNoteButton.setText("Update Note");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void createNote(String title, String content) {

            if (exists) {
                // UPDATE A NOTE

                Map updateMap = new HashMap();

                //Sets the note map to the new content entered by the user
                updateMap.put("title", newNoteTitle.getText().toString().trim());
                updateMap.put("content", newNoteContent.getText().toString().trim());
                updateMap.put("timestamp", ServerValue.TIMESTAMP);

                //Update that database entry
                notesDatabase.child(noteID).updateChildren(updateMap);

                //Allow user to see update was successful and finish the activity
                Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Create a new note
                //Creates reference to an auto-generated child location
                final DatabaseReference newNote = notesDatabase.push();

                //Used for storing key and value pairs
                final Map noteMap = new HashMap();

                //Add the note content to the note map
                noteMap.put("title", title);
                noteMap.put("content", content);
                noteMap.put("timestamp", ServerValue.TIMESTAMP);

                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Sets the newNote to the note map values, alerts user if successful or not
                        newNote.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(NewNoteActivity.this, "Note Successfully Created", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(NewNoteActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                });
                mainThread.start();
            }
    }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            super.onOptionsItemSelected(item);

            //Switch between different menu options
            switch (item.getItemId()) {
                case R.id.noteDelete:
                    if (exists) {
                        deleteNote();
                    } else {
                        Toast.makeText(this, "Nothing to delete", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

            return true;
        }

    private void deleteNote() {

        //Get note by id and remove it from database, alert user on completion
        notesDatabase.child(noteID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NewNoteActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(NewNoteActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
