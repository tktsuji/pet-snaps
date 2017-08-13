package blackbox.petsnaps;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ViewPostActivity extends AppCompatActivity implements CreateCommentFragment.SubmitButtonClick {

    private Context mContext;
    private String mPostKey;
    private DatabaseReference mFeedDBRef, mCommentsDBRef, mLikesDBRef, mUserDBRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currUser;
    private String postAuthorUid, postImage;

    private TextView titleTV, authorTV, descrpTV;
    private ImageView mainImageIV;
    private final static int statusBarHeight = 24;
    private final static int layoutMargins = 16;
    final static String COMMENT_FRAG = "Comment Fragment";

    private RecyclerView commentList;
    private FloatingActionButton mFab;
    private ProgressDialog progressDialog;
    private FirebaseRecyclerAdapter<CommentItem, CommentItemViewHolder> firebaseRecyclerAdapter;
    private boolean deleteComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post_container);
        setLayout();

        titleTV = (TextView) findViewById(R.id.title_tv);
        descrpTV = (TextView) findViewById(R.id.descrp_tv);
        authorTV = (TextView) findViewById(R.id.author_tv);
        mainImageIV = (ImageView) findViewById(R.id.main_image_iv);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currUser = mAuth.getCurrentUser();
        mFeedDBRef = FirebaseDatabase.getInstance().getReference().child("Main_Feed");
        mCommentsDBRef = FirebaseDatabase.getInstance().getReference().child("Comments");
        mLikesDBRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        mUserDBRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currUser.getUid());

        // SHOW POST
        mPostKey = getIntent().getStringExtra("post_key");
        mFeedDBRef.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String postTitle = (String) dataSnapshot.child("title").getValue();
                String postDescrp = (String) dataSnapshot.child("descrp").getValue();
                postImage = (String) dataSnapshot.child("image").getValue();
                String postAuthor = (String) dataSnapshot.child("username").getValue();
                postAuthorUid = (String) dataSnapshot.child("uid").getValue();
                titleTV.setText(postTitle);
                descrpTV.setText(postDescrp);
                CharSequence author = "posted by " + postAuthor;
                authorTV.setText(author);
                Picasso.with(ViewPostActivity.this).load(postImage).into(mainImageIV);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // SHOW COMMENTS
        commentList = (RecyclerView) findViewById(R.id.comment_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        commentList.setLayoutManager(mLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(commentList.getContext(),
                mLayoutManager.getOrientation());
        commentList.addItemDecoration(mDividerItemDecoration);

        // MAKE COMMENT
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentFragment();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mContext = this;
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<CommentItem, CommentItemViewHolder>(
                CommentItem.class,
                R.layout.comment_item,
                CommentItemViewHolder.class, mCommentsDBRef.orderByChild("postKey").equalTo(mPostKey)
        ) {
            @Override
            protected void populateViewHolder(CommentItemViewHolder viewHolder, CommentItem model, int position) {
                viewHolder.setUsername(model.getUsername() + ": ");
                viewHolder.setMessage(model.getMessage());

                if (currUser.getUid().equals(model.getUid())) {
                    viewHolder.setDeleteButton(mContext, true, getRef(position).getKey(), mPostKey);
                }
            }
        };
        commentList.setNestedScrollingEnabled(false);
        commentList.setAdapter(firebaseRecyclerAdapter);
    }

    private void showCommentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        CreateCommentFragment commentFrag = new CreateCommentFragment();
        commentFrag.show(fm, COMMENT_FRAG);
    }

    public void submitButtonClick(String message) {
        progressDialog.setMessage("Submitting comment...");
        progressDialog.show();
        final String mssg = message.replace("\n", " ");
        if (mAuth.getCurrentUser() != null) {
            final DatabaseReference newComment = mCommentsDBRef.push();
            mUserDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newComment.child("message").setValue(mssg);
                    newComment.child("uid").setValue(currUser.getUid());
                    newComment.child("username").setValue(dataSnapshot.child("username").getValue());
                    newComment.child("postKey").setValue(mPostKey);
                    if (firebaseRecyclerAdapter != null) {
                        firebaseRecyclerAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("SUBMIT COMMENT", "onCancelled()");
                }
            });
            incrementCommentCount();
        }

        progressDialog.dismiss();
    }

    private void incrementCommentCount() {
        DatabaseReference numCommentsRef = mFeedDBRef.child(mPostKey).child("numComments");
        numCommentsRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                System.out.println("Transaction completed");
            }
        });
    }

    private void deletePost() {
        // DELETE ASSOCIATED LIKES
        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mLikesDBRef.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getValue() != null && snapshot.getValue().equals("true")) {
                        usersRef.child(snapshot.getKey()).child("Favorites").child(mPostKey).removeValue();
                    }
                }
                mLikesDBRef.child(mPostKey).removeValue();
                Log.d("ViewPostActivity", "DELETED LIKES");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // DELETE ASSOCIATED COMMENTS
        deleteComments = true;
        mCommentsDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (deleteComments) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // loop through snapshot of every comment
                        String postKey = (String) snapshot.child("postKey").getValue();
                        if (postKey != null) {
                            if (postKey.equals(mPostKey))
                                snapshot.getRef().removeValue();
                        }
                    }
                    deleteComments = false;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        Log.d("ViewPostActivity", "DELETED COMMENTS");

        // DELETE ASSOCIATED IMAGE FROM STORAGE
        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        StorageReference photoRef = mStorage.getReferenceFromUrl(postImage);
        photoRef.delete();
        Log.d("ViewPostActivity", "DELETED IMG FROM STORAGE");

        // DELETE FROM CATEGORIES
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dogRef = dbRef.child("Dog_Posts");
        DatabaseReference catRef = dbRef.child("Cat_Posts");
        DatabaseReference birdRef = dbRef.child("Bird_Posts");
        DatabaseReference rabbitRef = dbRef.child("Rabbit_Posts");
        DatabaseReference reptileRef = dbRef.child("Reptile_Posts");
        DatabaseReference rodentRef = dbRef.child("Rodent_Posts");
        DatabaseReference postKeysRef = dbRef.child("Post_Keys");
        dogRef.child(mPostKey).removeValue();
        catRef.child(mPostKey).removeValue();
        birdRef.child(mPostKey).removeValue();
        rabbitRef.child(mPostKey).removeValue();
        reptileRef.child(mPostKey).removeValue();
        rodentRef.child(mPostKey).removeValue();
        postKeysRef.child(mPostKey).removeValue();
        Log.d("ViewPostActivity", "DELETED CATEGORIES");

        // DELETE ACTUAL POST DATA
        mFeedDBRef.child(mPostKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("ViewPostActivity", "DELETED POST ITSELF");
                //Intent i = new Intent(ViewPostActivity.this, MainActivity.class);
                //startActivity(i);
                finish();
            }
        });

    }

    private void setLayout() {
        // Calculate ActionBar height
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            System.out.println("ACTION BAR HEIGHT: " + actionBarHeight);
        }
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenHeight = size.y;
        LinearLayout layout = (LinearLayout)findViewById(R.id.post_chunk);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = screenHeight - actionBarHeight - statusBarHeight - layoutMargins;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (currUser.getUid().equals(postAuthorUid)) {
            getMenuInflater().inflate(R.menu.post_options, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.delete_post:
                deletePost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

