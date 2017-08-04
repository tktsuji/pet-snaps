package blackbox.petsnaps.FilterFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    FirebaseUser currentUser;

    protected View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_posts, container, false);
        postList = (RecyclerView) mView.findViewById(R.id.post_list);
        postList.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        mMainFeedRef = FirebaseDatabase.getInstance().getReference().child("Main_Feed");
        mLikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return mView;
    }

    protected void superPopViewHolder(PostItemViewHolder viewHolder, PostItem model, int position, final String post_key) {
        viewHolder.setTitle(model.getTitle());
        viewHolder.setImage(getActivity(), model.getImage());
        viewHolder.setHeartIcon(getActivity(), post_key);
        viewHolder.setNumComments(post_key);

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
}
