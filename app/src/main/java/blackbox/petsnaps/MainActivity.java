package blackbox.petsnaps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import blackbox.petsnaps.FilterFragments.MyPostsFragment;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private String currUsername;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsersRef;
    private ProgressDialog progressDialog;

    private TextView welcomeTV;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FrameLayout fragmentContainer;
    private Toolbar toolbar;

    private static int currState = 0; // 0:MainFeed WITH TABS | 1:MyPosts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MAIN FEED", "onCreate()");
        setContentView(R.layout.activity_main_feed);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            // USER NEEDS TO LOG IN FIRST
            Log.d("MAINACTIVITY", "Sending to Login");
            progressDialog.dismiss();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        else {
            fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);
            setTabLayout();
            setDrawerLayout();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MAIN FEED", "onStart()");
        if (currState == 0)
            swapInMainFeed();
        else if (currState == 1)
            swapInMyPosts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_create_post) {
            Intent i = new Intent(this, CreatePostActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_feed) {
            if (currState != 0) {
                swapInMainFeed();
                currState = 0;
            }
        } else if (id == R.id.nav_my_posts) {
            if (currState != 1) {
                swapInMyPosts();
                currState = 1;
            }
        }
        else if (id == R.id.nav_sign_out) {
            firebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

   private void swapInMainFeed() {
       tabLayout.getTabAt(0);
       fragmentContainer.setVisibility(View.GONE);
       viewPager.setVisibility(View.VISIBLE);
       tabLayout.setVisibility(View.VISIBLE);

       CharSequence title = "Feed";
       if (getSupportActionBar() != null)
        getSupportActionBar().setTitle(title);
   }

   private void swapInMyPosts() {
       MyPostsFragment myPostsFrag = new MyPostsFragment();
       FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
       transaction.replace(R.id.fragment_container, myPostsFrag)
               .addToBackStack(null)
               .commit();
       fragmentContainer.setVisibility(View.VISIBLE);
       viewPager.setVisibility(GONE);
       tabLayout.setVisibility(GONE);

       CharSequence title = "My Posts";
       if (getSupportActionBar() != null)
           getSupportActionBar().setTitle(title);
   }

   private void setTabLayout() {
       tabLayout = (TabLayout) findViewById(R.id.tab_layout);
       tabLayout.addTab(tabLayout.newTab().setText("All"));
       tabLayout.addTab(tabLayout.newTab().setText("Dogs"));
       tabLayout.addTab(tabLayout.newTab().setText("Cats"));
       tabLayout.addTab(tabLayout.newTab().setText("Birds"));
       tabLayout.addTab(tabLayout.newTab().setText("Rabbits"));
       tabLayout.addTab(tabLayout.newTab().setText("Reptiles"));
       tabLayout.addTab(tabLayout.newTab().setText("Rodents"));

       viewPager = (ViewPager) findViewById(R.id.pager);
       final blackbox.petsnaps.PagerAdapter pagerAdapter
               = new blackbox.petsnaps.PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
       viewPager.setAdapter(pagerAdapter);
       viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
       tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
           @Override
           public void onTabSelected(TabLayout.Tab tab) {
               viewPager.setCurrentItem(tab.getPosition());
           }
           @Override
           public void onTabUnselected(TabLayout.Tab tab) {}
           @Override
           public void onTabReselected(TabLayout.Tab tab) {}
       });
   }

   private void setDrawerLayout() {
       DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
               this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
       drawer.setDrawerListener(toggle);
       toggle.syncState();
       NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
       navigationView.setNavigationItemSelectedListener(this);
       View headerView = navigationView.getHeaderView(0);
       welcomeTV = (TextView) headerView.findViewById(R.id.welcome_tv);

       mDatabase = FirebaseDatabase.getInstance();
       mUsersRef = mDatabase.getReference().child("Users");
       mUsersRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               currUsername = (String) dataSnapshot.child("username").getValue();
               System.out.println("TEST: " + currUsername);
               CharSequence welcome = "Hello, " + currUsername + "!";
               welcomeTV.setText(welcome);
               progressDialog.dismiss();
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {
               progressDialog.dismiss();
           }
       });
   }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // DO NOTHING
        }
    }

}
