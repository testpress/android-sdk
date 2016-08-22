package in.testpress.samples.exam;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.facebook.login.LoginManager;

import in.testpress.core.TestpressSdk;
import in.testpress.exam.TestpressExam;
import in.testpress.samples.R;
import in.testpress.samples.core.TestpressCoreSampleActivity;

public class NavigationDrawerActivity extends AppCompatActivity {

    public static final int AUTHENTICATE_REQUEST_CODE = 1111;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private int selectedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (!TestpressSdk.hasActiveSession(this)) {
           navigationView.getMenu().getItem(2).setVisible(false);
        }
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        displayHome();
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.home:
                displayHome();
                selectedItem = 0;
                break;
            case R.id.exams:
                if (TestpressSdk.hasActiveSession(this)) {
                    displayExams();
                } else {
                    Intent intent = new Intent(NavigationDrawerActivity.this, TestpressCoreSampleActivity.class);
                    startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
                }
                selectedItem = 1;
                break;
            case R.id.logout:
                TestpressSdk.clearActiveSession(this);
                LoginManager.getInstance().logOut();
                finish();
                break;
        }
        drawerLayout.closeDrawers();
    }

    private void displayHome() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void displayExams() {
        TestpressExam.show(this, R.id.fragment_container, TestpressSdk.getTestpressSession(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            displayExams();
            navigationView.getMenu().getItem(2).setVisible(true);
        }
    }

    @Override
    public void onBackPressed() {
        //check drawer is open
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else if (selectedItem == 0) { //backpress from home will close activity
            super.onBackPressed();
        } else {
            //backpress  from other fragments will go to home fragment
            displayHome();
            navigationView.getMenu().getItem(0).setChecked(true);
            selectedItem = 0;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }
}
