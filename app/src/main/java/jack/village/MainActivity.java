package jack.village;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.EventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseAuth auth;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.nav);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        NavigationView navigationView = findViewById(R.id.nav_view);

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null)
        {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.navigation_menu);
        } else
        {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.navigation_menu_guest);
        }

        View header=navigationView.getHeaderView(0);
        FirebaseUser user = auth.getCurrentUser();
        TextView showEmail = header.findViewById(R.id.user_email);
        String email;

        if(user != null && user.getEmail() != null) {
            email = user.getEmail();
        }
        else{
            email = "Guest";
        }
        showEmail.setText(email);

        navigationView.setNavigationItemSelectedListener(this);

        if(fragment == null) {
            fragment = new TabHome();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }


    @Override
    public void onResume(){
        fragment = new TabHome();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();

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
            fragment = new TabNotes();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        else if (id == R.id.podcast && !item.isChecked()) {
            fragment = new TabPodcast();
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
            fragment = new TabForum();
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
