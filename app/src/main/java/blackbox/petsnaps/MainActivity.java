package blackbox.petsnaps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private String currUsername;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsersRef;

    private RecyclerView postList;
    private ProgressDialog progressDialog;
    private TextView welcomeTV;

    private boolean processLike = false;
    private static int currFragment = 0; // 0:MainFeed | 1:MyPosts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MAIN FEED", "onCreate()");
        setContentView(R.layout.activity_main_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
            mDatabase = FirebaseDatabase.getInstance();
            mUsersRef = mDatabase.getReference().child("Users");

            // SET UP DRAWER LAYOUT
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            View headerView = navigationView.getHeaderView(0);
            welcomeTV = (TextView) headerView.findViewById(R.id.welcome_tv);

            mUsersRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currUsername = (String) dataSnapshot.child("username").getValue();
                    System.out.println("TEST: " + currUsername);
                    CharSequence welcome = "Hello, " + currUsername + "!";
                    welcomeTV.setText(welcome);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MAIN FEED", "onStart()");
        if (currFragment == 0)
            swapInMainFeed();
        else if (currFragment == 1)
            swapInMyPosts();
        progressDialog.dismiss();

        /* TESTING ---------------------------------- */
        new VisionAnalysisRetriever();
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
            swapInMainFeed();
            currFragment = 0;
        } else if (id == R.id.nav_my_posts) {
            swapInMyPosts();
            currFragment = 1;
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
       AllPostsFragment allPostsFrag = new AllPostsFragment();
       FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
       transaction.replace(R.id.fragment_container, allPostsFrag)
               .addToBackStack(null)
               .commit();

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

       CharSequence title = "My Posts";
       if (getSupportActionBar() != null)
           getSupportActionBar().setTitle(title);
   }

}
