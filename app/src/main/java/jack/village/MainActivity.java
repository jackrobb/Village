package jack.village;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //Set drawer layout to display navigation items
        drawerLayout = findViewById(R.id.nav);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users");

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //Hide title from the action bar
        if(getSupportActionBar() !=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);

        //If the user is logged in show the logged in navigation drawer
        if(auth.getCurrentUser() != null)
        {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.navigation_menu);

        } else
            //If its a guest user show the guest navigation drawer
        {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.navigation_menu_guest);
        }

        //Set the header to display the user details
        View header=navigationView.getHeaderView(0);
        final TextView showEmail = header.findViewById(R.id.user_email);
        ImageView userImage = header.findViewById(R.id.imageView);
        final String userEmail;

        //If user is logged in show user name and image
        if(user != null) {
            userEmail = auth.getCurrentUser().getEmail();
            final String user_id = auth.getCurrentUser().getUid();

            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(user_id).child("Name").getValue() != null) {
                        String userName = dataSnapshot.child(user_id).child("Name").getValue().toString();
                        showEmail.setText(userName);
                    }else {
                        showEmail.setText(R.string.error);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            String hash = MD5Util.md5Hex(userEmail);

            String icon = "https://www.gravatar.com/avatar/" + hash +"s=2048";

            Glide.with(getApplicationContext())
                    .load(icon)
                    .apply(new RequestOptions()
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .into(userImage);
        }
        else{
            //If no user logged in set user name to guest
            showEmail.setText(R.string.Guest);
        }


        navigationView.setNavigationItemSelectedListener(this);

        //If no fragment is selected go to the home fragment
        if(fragment == null) {
            fragment = new TabHome();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();

        //Set route for each navigation item
        if (id == R.id.home && !item.isChecked()) {
            fragment = new TabHome();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        else if (id == R.id.contact && !item.isChecked()) {
            fragment = new TabContact();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        else if (id == R.id.notes && !item.isChecked()) {
            fragment = new NotesTab();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        else if (id == R.id.podcast && !item.isChecked()) {
            fragment = new PodcastTab();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        else if (id == R.id.events && !item.isChecked()) {
            fragment = new TabEvents();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        else if (id == R.id.forum && !item.isChecked()) {
            fragment = new ForumTab();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        else if (id == R.id.donate && !item.isChecked()) {
            fragment = new TabDonate();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

        else if (id == R.id.settings) {
            startActivity(new Intent(this, UpdateAccountActivity.class));
        }

        else if (id == R.id.createAccount){
            startActivity(new Intent(this, SignUpActivity.class));
        }

        else if(id == R.id.login){
            startActivity(new Intent(this, LoginActivity.class));
        }

        item.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
