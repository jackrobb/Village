package jack.village;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity{

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initiate custom toolbar for tabs
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create an instance of the tab layout which contains tab headers
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        // Set the text for each tab header
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_label1));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_label2));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_label3));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_label4));

        // Tabs fill full layout
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Allows users to flip left and right between tabs
        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        // Setting a listener for user interactions
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.guest_menu, menu);
        }else {
            //Used to initiate menu containing logout and update button
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Switch for different menu items
        switch(item.getItemId()){
            case R.id.menuLogout:

                //Get current user and sign them out, finishing all activities to prevent users going back without signing in
                FirebaseAuth.getInstance().signOut();
                finishAffinity();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;

            case R.id.menuLogin:
                startActivity(new Intent(this, LoginActivity.class));
                break;

                //Open update account activity
            case R.id.menuChange:
                startActivity(new Intent(this, UpdateAccountActivity.class));
                break;

            case R.id.menuCreate:
                startActivity(new Intent(this, SignUpActivity.class));
                break;
        }

        return true;
    }
}
