package blackbox.petsnaps.FilterFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import blackbox.petsnaps.PostItem;
import blackbox.petsnaps.PostItemViewHolder;
import blackbox.petsnaps.R;
import blackbox.petsnaps.ViewPostActivity;

public abstract class BaseFilterFragment extends Fragment {
    RecyclerView postList;
    boolean processLike = false;
    DatabaseReference mMainFeedRef;
    DatabaseReference mLikesRef;
    DatabaseReference mUsersRef;
    FirebaseUser currentUser;

    protected View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_posts, container, false);
        postList = (RecyclerView) mView.findViewById(R.id.post_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        postList.setLayoutManager(gridLayoutManager);

        mMainFeedRef = FirebaseDatabase.getInstance().getReference().child("Main_Feed");
        mLikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        postList.scrollToPosition(0);
    }

    protected void superPopViewHolder(final PostItemViewHolder viewHolder, final String post_key) {
        Log.d("BASEFILTERFRAG", "superPopViewHolder()");
        DatabaseReference postRef = mMainFeedRef.child(post_key);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.setTitle((String) dataSnapshot.child("title").getValue());
                viewHolder.setImage(getActivity(), (String) dataSnapshot.child("image").getValue());
                viewHolder.setHeartIcon(getActivity(), post_key);
                viewHolder.setNumComments(post_key);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ViewPostActivity.class);
                i.putExtra("post_key", post_key);
                startActivity(i);
            }
        });

        viewHolder.heartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processLike = true;
                mLikesRef.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (processLike) {
                            if (dataSnapshot.hasChild(currentUser.getUid())) {
                                // USER HAS UNLIKED A POST
                                mLikesRef.child(post_key).child(currentUser.getUid()).removeValue();
                                mUsersRef.child(currentUser.getUid()).child("Favorites").child(post_key).removeValue();
                                mMainFeedRef.child(post_key).child("numLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long numLikes = (long) dataSnapshot.getValue();
                                        mMainFeedRef.child(post_key).child("numLikes").setValue(numLikes + 1);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                processLike = false;
                            } else {
                                // USER HAS LIKED A POST
                                mLikesRef.child(post_key).child(currentUser.getUid()).setValue("true");
                                mUsersRef.child(currentUser.getUid()).child("Favorites").child(post_key).setValue("true");
                                mMainFeedRef.child(post_key).child("numLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long numLikes = (long) dataSnapshot.getValue();
                                        mMainFeedRef.child(post_key).child("numLikes").setValue(numLikes - 1);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
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
}
