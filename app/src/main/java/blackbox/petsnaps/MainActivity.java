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
    private DatabaseReference mMainFeedRef;
    private DatabaseReference mUsersRef;
    private DatabaseReference mLikesRef;

    private RecyclerView postList;
    private ProgressDialog progressDialog;

    private boolean processLike = false;

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
            progressDialog.dismiss();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        mDatabase = FirebaseDatabase.getInstance();
        mMainFeedRef = mDatabase.getReference().child("Main_Feed");
        mUsersRef = mDatabase.getReference().child("Users");
        mLikesRef = mDatabase.getReference().child("Likes");


       // SET UP DRAWER LAYOUT
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mUsersRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currUsername = (String) dataSnapshot.child("username").getValue();
                TextView welcomeTV = (TextView) findViewById(R.id.welcome_tv);
                CharSequence welcome = "Hello, " + currUsername + "!";
                welcomeTV.setText(welcome);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // SET UP RECYCLER VIEW
        /*postList = (RecyclerView) findViewById(R.id.post_list);
        postList.setHasFixedSize(true);
        postList.setLayoutManager(new GridLayoutManager(this, 2));*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        // POPULATE RECYCLER VIEW WITH POSTS FROM THE DATABASE
        Log.d("MAIN FEED", "onStart()");
        AllPostsFragment allPostsFrag = new AllPostsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, allPostsFrag)
                .addToBackStack(null)
                .commit();
        /*FirebaseRecyclerAdapter<PostItem, PostItemViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostItem, PostItemViewHolder>(
            PostItem.class,
            R.layout.post_item,
            PostItemViewHolder.class, mMainFeedRef.orderByChild("reverse_timestamp")
        ) {
            @Override
            protected void populateViewHolder(PostItemViewHolder viewHolder, PostItem model, int position) {
                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setHeartIcon(getApplicationContext(), post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this, ViewPostActivity.class);
                        i.putExtra("post_key", post_key);
                        startActivity(i);
                    }
                });

                viewHolder.heartIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "You liked a post.", Toast.LENGTH_SHORT).show();
                        processLike = true;
                        mLikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (processLike) {
                                    if (dataSnapshot.child(post_key).hasChild(currentUser.getUid())) {
                                        // USER HAS UNLIKED A POST
                                        mLikesRef.child(post_key).child(currentUser.getUid()).removeValue();
                                        processLike = false;
                                    } else {
                                        // USER HAS LIKED A POST
                                        mLikesRef.child(post_key).child(currentUser.getUid()).setValue("true");
                                        processLike = false;
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

        postList.setAdapter(firebaseRecyclerAdapter); */
        progressDialog.dismiss();
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            //setMyPosts();


        } else if (id == R.id.nav_slideshow) {

        }
        else if (id == R.id.nav_send) {
            firebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setMyPosts() {
        FirebaseRecyclerAdapter<PostItem, PostItemViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostItem, PostItemViewHolder>(
                PostItem.class,
                R.layout.post_item,
                PostItemViewHolder.class, mMainFeedRef.orderByChild("uid").equalTo(currentUser.getUid())
        ) {
            @Override
            protected void populateViewHolder(PostItemViewHolder viewHolder, PostItem model, int position) {
                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setHeartIcon(getApplicationContext(), post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this, ViewPostActivity.class);
                        i.putExtra("post_key", post_key);
                        startActivity(i);
                    }
                });

                viewHolder.heartIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "You liked a post.", Toast.LENGTH_SHORT).show();
                        processLike = true;
                        mLikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (processLike) {
                                    if (dataSnapshot.child(post_key).hasChild(currentUser.getUid())) {
                                        // USER HAS UNLIKED A POST
                                        mLikesRef.child(post_key).child(currentUser.getUid()).removeValue();
                                        processLike = false;
                                    } else {
                                        // USER HAS LIKED A POST
                                        mLikesRef.child(post_key).child(currentUser.getUid()).setValue("true");
                                        processLike = false;
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
        postList.setAdapter(firebaseRecyclerAdapter);
    }

}
