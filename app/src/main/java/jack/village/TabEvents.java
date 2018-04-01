package jack.village;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
public class TabEvents extends Fragment implements View.OnClickListener{

    private FloatingActionButton createPost;
    private RecyclerView eventList;
    private DatabaseReference eventsDB;
    private FirebaseAuth auth;
    private boolean isGoing = false;
    private DatabaseReference goingDB;
    private String admin = "jrobb6696@gmail.com";

    public TabEvents() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_events, container, false);

        super.onCreate(savedInstanceState);

        //Get reference to Events and goingIB eventsDB
        eventsDB = FirebaseDatabase.getInstance().getReference().child("Events");
        goingDB = FirebaseDatabase.getInstance().getReference().child("Going");

        //Keep the data synced to save user data and improve load times
        eventsDB.keepSynced(true);
        goingDB.keepSynced(true);

        createPost = view.findViewById(R.id.createPost);
        createPost.setOnClickListener(this);

        eventList = view.findViewById(R.id.eventList);

        //Set layout manager to control the flow of the events
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);

        eventList.setHasFixedSize(true);
        eventList.setLayoutManager(linearLayoutManager);

        //Get reference to auth eventsDB
        auth = FirebaseAuth.getInstance();

        return view;
    }

    public void onResume(){
        super.onResume();
        //Only allow admin to access the new events button
        if(auth.getCurrentUser() == null){
            createPost.hide();
        }else if (auth.getCurrentUser().getEmail().equals(admin)) {
            createPost.show();
        }else{
            createPost.hide();
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        //Order events by time
        Query query = eventsDB.orderByChild("timestamp");

        FirebaseRecyclerAdapter<EventModel, EventViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<EventModel, EventViewHolder>(
                EventModel.class,
                R.layout.single_event_layout,
                EventViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final EventViewHolder viewHolder, final EventModel model, int position) {

                //Set event_id to the current postion key
                final String event_id = getRef(position).getKey();

                //For each event item set all the content
                viewHolder.setTitle(model.getTitle());
                viewHolder.setContent(model.getContent());
                viewHolder.setDate(model.getDate());
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
                viewHolder.setGoingDB(event_id);
                viewHolder.setGoingCount(event_id);

                //Long on click listener to allow admin to delete the event
                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        if (auth.getCurrentUser() != null) {
                            //User must be the creator of the comment to delete it
                            if (auth.getCurrentUser().getEmail().equals(admin)) {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Delete Event?")
                                        .setCancelable(false)
                                        .setNegativeButton("Cancel", null)
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                goingDB.child(event_id).removeValue();
                                                eventsDB.child(event_id).removeValue();
                                            }
                                        })
                                        .show();
                            }
                        }
                        return false;
                    }
                });


                //Set on click listener for the going button
                viewHolder.goingIB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Boolean to prevent issue with live eventsDB
                        isGoing = true;

                        goingDB.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                //If true allow user to select or deselect icon
                                if (isGoing) {
                                    //If the user is already going to the event set remove them
                                    if (dataSnapshot.child(event_id).hasChild(auth.getCurrentUser().getUid())) {

                                        goingDB.child(event_id).child(auth.getCurrentUser().getUid()).removeValue();
                                        isGoing = false;

                                    } else {
                                        //If the user is not going to the event then add their unique ID
                                        goingDB.child(event_id).child(auth.getCurrentUser().getUid()).setValue(auth.getCurrentUser().getEmail());
                                        isGoing = false;
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }


            };

                eventList.setAdapter(firebaseRecyclerAdapter);
        }



    public static class EventViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ImageButton goingIB;
        ImageButton optionsIB;
        DatabaseReference goingDB;
        DatabaseReference goingCountDB;
        FirebaseAuth auth;
        TextView goingCount;
        Context context;

        public EventViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            goingIB = mView.findViewById(R.id.going);

            goingDB = FirebaseDatabase.getInstance().getReference().child("Going");
            auth = FirebaseAuth.getInstance();

            goingCount = mView.findViewById(R.id.goingCount);
            optionsIB = mView.findViewById(R.id.options);

            context = mView.getContext();

            goingDB.keepSynced(true);
        }

        public void setGoingDB(final String event_id) {
            goingDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    //Ensure user is logged in
                    if (auth.getCurrentUser() != null) {
                        //If the users id has been added to the DB then they are going - set icon colour to blue
                        if (dataSnapshot.child(event_id).hasChild(auth.getCurrentUser().getUid())) {
                            goingIB.setColorFilter(Color.rgb(143, 177, 186));
                        } else {
                            //Else the user has unselected the going icon - set icon to default grey
                            goingIB.setColorFilter(Color.rgb(211, 211, 211));
                        }
                    } else {
                        //If the user is not logged in, display dialogue box telling them they need an account
                        goingIB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new android.support.v7.app.AlertDialog.Builder(context)
                                        .setTitle("Sign up for Village")
                                        .setMessage("Sign up to attend this event")
                                        .setCancelable(false)
                                        .setNegativeButton("Cancel", null)
                                        .setNeutralButton("Already have an account? Sign in", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(context, LoginActivity.class);
                                                context.startActivity(intent);
                                            }
                                        })
                                        .setPositiveButton("Sign Up", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(context, SignUpActivity.class);
                                                context.startActivity(intent);
                                            }
                                        })
                                        .show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        public void setGoingCount(final String event_id){
            //Get the number of children from the going eventsDB
            goingCountDB = FirebaseDatabase.getInstance().getReference().child("Going").child(event_id);
            goingCountDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String goingCounter = String.valueOf(dataSnapshot.getChildrenCount());
                    if(!goingCounter.isEmpty()){
                        //Set text to display the number of people going
                        goingCount.setText(goingCounter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title){
            //Set title to title pulled from eventsDB
            TextView eventTitle = mView.findViewById(R.id.eventTitle);
            eventTitle.setText(title);
        }

        public void setDate(String date){
            //Set title to title pulled from eventsDB
            TextView eventDate = mView.findViewById(R.id.eventDate);
            eventDate.setText(date);
        }

        public void setContent(String content){
            //Set content from content pulled from eventsDB
            final TextView eventContent = mView.findViewById(R.id.eventContent);
            eventContent.setText(content);
        }

        public void setImage(Context context, String image){
            //Set image from image pulled from eventsDB
            ImageView eventImage = mView.findViewById(R.id.eventImage);
            Glide.with(context)
                    .load(image)
                    .apply(new RequestOptions()
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .into(eventImage);


        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createPost:
                startActivity(new Intent(getActivity(), EventActivityNew.class));
                break;
        }
    }


}
