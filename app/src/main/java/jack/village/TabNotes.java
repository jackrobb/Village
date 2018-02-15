package jack.village;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.widget.GridLayout.VERTICAL;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabNotes extends Fragment implements View.OnClickListener{

        private FloatingActionButton createNote;
        private RecyclerView noteList;
        private LinearLayoutManager linearLayoutManager;

        private FirebaseAuth auth;
        private DatabaseReference notesDatabase;

        private TextView logged;

        ProgressBar progressBar;


    public TabNotes() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_notes , container, false);

            createNote = view.findViewById(R.id.createNote);
            createNote.setOnClickListener(this);

            noteList = view.findViewById(R.id.noteList);

            progressBar = view.findViewById(R.id.progressbar);

            logged = view.findViewById(R.id.loggedIn);

            //3 Notes per vertical line in order by newest first
            linearLayoutManager = new LinearLayoutManager(getActivity(), VERTICAL, true);
            linearLayoutManager.setStackFromEnd(true);

            //set note list to a fixed size and assign it to linear layout manager
            noteList.setHasFixedSize(true);
            noteList.setLayoutManager(linearLayoutManager);

            //Ensure the user is logged in, if they are get all their current notes
            auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                if (auth.getCurrentUser().isAnonymous()) {
                    createNote.hide();
                    logged.setText(R.string.logged);
                } else {
                    notesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(auth.getCurrentUser().getUid());

                    //Allow access to notes offline
                    notesDatabase.keepSynced(true);

                    loadData();
                }
            }


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);

        //Order the notes by time created
        Query query = notesDatabase.orderByChild("timestamp");

        //Recycler Adaptor uses NoteModel class and th single note layout
        FirebaseRecyclerAdapter<NoteModel, NoteViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NoteModel, NoteViewHolder>(

                //Set the FirebaseRecyclerAdaptor to use note model and note view holder class and single note layout
                NoteModel.class,
                R.layout.single_note_layout,
                NoteViewHolder.class,
                query

        ) {
            @Override
            protected void populateViewHolder(final NoteViewHolder viewHolder, NoteModel model, int position) {
                final String noteId = getRef(position).getKey();

                progressBar.setVisibility(View.GONE);

                //Displays all users current notes
                notesDatabase.child(noteId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("timestamp")) {
                            String title = dataSnapshot.child("title").getValue().toString();
                            String content = dataSnapshot.child("content").getValue().toString();
                            String timestamp = dataSnapshot.child("timestamp").getValue().toString();

                            viewHolder.setNoteTitle(title);
                            viewHolder.setNoteContent(content);

                            //Sets the time for the current notes
                            GetTime getTime = new GetTime();
                            viewHolder.setNoteTime(getTime.getTime(Long.parseLong(timestamp)));

                            //Each note is placed within a note card which is clickable, when the user selects it, it open the new note activity allowing them to edit it.
                            viewHolder.noteCard.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent note = new Intent(getActivity(),  NewNoteActivity.class);
                                    note.putExtra("noteId", noteId);
                                    startActivity(note);
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        //Fills the note list with the adaptor content
        noteList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createNote:
                startActivity(new Intent(getActivity(), NewNoteActivity.class));
                break;
        }
    }

}
