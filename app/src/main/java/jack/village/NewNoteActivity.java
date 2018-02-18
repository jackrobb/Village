package jack.village;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

    private EditText newNoteTitle;
    private EditText newNoteContent;

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
            exists = !noteID.trim().equals("");

        } catch (Exception e) {
            e.printStackTrace();
        }

        newNoteTitle = findViewById(R.id.newNoteTitle);
        newNoteContent = findViewById(R.id.newNoteContent);
        Toolbar toolbar = findViewById(R.id.newNoteToolBar);

        //Set toolbar to custom toolbar
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Obtain an instance of the FirebaseAuth class
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //Gets reference to the current users notes location of the database
        //Creates database table for Notes
        if(mAuth.getCurrentUser() != null ) {
            notesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid());
        }
        //Calls method put data
        putData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String title = newNoteTitle.getText().toString().trim();
        String content = newNoteContent.getText().toString().trim();
        createNote(title, content);
        finish();
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
                final Map newMap = new HashMap();

                //Add the note content to the note map
                newMap.put("title", title);
                newMap.put("content", content);
                newMap.put("timestamp", ServerValue.TIMESTAMP);

                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Sets the newNote to the note map values, alerts user if successful or not
                        newNote.setValue(newMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                        new AlertDialog.Builder(this)
                                .setMessage("Are you sure you want to delete this note?")
                                .setCancelable(false)
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        NewNoteActivity.this.deleteNote();
                                    }
                                })
                                .show();
                    } else {
                        Toast.makeText(this, "Nothing to delete", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case android.R.id.home:
                    onBackPressed();
                    break;
            }

            return true;
        }

    private void deleteNote() {
        finish();
        //Get note by id and remove it from database, alert user on completion
        notesDatabase.child(noteID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NewNoteActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NewNoteActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
