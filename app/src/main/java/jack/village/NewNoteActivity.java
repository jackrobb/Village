package jack.village;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        getMenuInflater().inflate(R.menu.note_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        try {
            noteID = getIntent().getStringExtra("noteId");

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

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        notesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid());

        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = newNoteTitle.getText().toString().trim();
                String content = newNoteContent.getText().toString().trim();

                if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)){
                    createNote(title, content);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Complete All Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        putData();
    }

    private void putData() {

        if (exists) {
            notesDatabase.child(noteID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")) {
                        String title = dataSnapshot.child("title").getValue().toString();
                        String content = dataSnapshot.child("content").getValue().toString();

                        newNoteTitle.setText(title);
                        newNoteContent.setText(content);
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
                updateMap.put("title", newNoteTitle.getText().toString().trim());
                updateMap.put("content", newNoteContent.getText().toString().trim());
                updateMap.put("timestamp", ServerValue.TIMESTAMP);

                notesDatabase.child(noteID).updateChildren(updateMap);

                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // CREATE A NEW NOTE
                final DatabaseReference newNote = notesDatabase.push();

                final Map noteMap = new HashMap();
                noteMap.put("title", title);
                noteMap.put("content", content);
                noteMap.put("timestamp", ServerValue.TIMESTAMP);

                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        newNote.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(NewNoteActivity.this, "Note added to database", Toast.LENGTH_SHORT).show();
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

            switch (item.getItemId()) {
                case android.R.id.home:
                    finish();
                    break;
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

        notesDatabase.child(noteID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NewNoteActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                    noteID = "no";
                    finish();
                } else {
                    Toast.makeText(NewNoteActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
