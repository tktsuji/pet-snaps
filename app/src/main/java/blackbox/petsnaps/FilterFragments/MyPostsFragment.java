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
import android.widget.Toast;

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

public class MyPostsFragment extends BaseFilterFragment {

    private FirebaseRecyclerAdapter<PostItem, PostItemViewHolder> firebaseRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MYPOSTSFRAGMENT", "ON START");
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostItem, PostItemViewHolder>(
                PostItem.class,
                R.layout.post_item,
                PostItemViewHolder.class, mMainFeedRef.orderByChild("uid").equalTo(currentUser.getUid())
        ) {
            @Override
            protected void populateViewHolder(PostItemViewHolder viewHolder, PostItem model, int position) {
                final String post_key = getRef(position).getKey();
                superPopViewHolder(viewHolder, post_key);
            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
        Log.d("MYPOSTSFRAG", "ON DESTROY");
    }



}
