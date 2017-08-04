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
import com.google.firebase.database.ValueEventListener;

import blackbox.petsnaps.PostItem;
import blackbox.petsnaps.PostItemViewHolder;
import blackbox.petsnaps.R;
import blackbox.petsnaps.ViewPostActivity;


public class AllPostsFragment extends BaseFilterFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<PostItem, PostItemViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostItem, PostItemViewHolder>(
                PostItem.class,
                R.layout.post_item,
                PostItemViewHolder.class, mMainFeedRef.orderByChild("reverse_timestamp")
        ) {
            @Override
            protected void populateViewHolder(PostItemViewHolder viewHolder, PostItem model, int position) {
                final String post_key = getRef(position).getKey();
                superPopViewHolder(viewHolder, model, position, post_key);
            }
        };

        postList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ALLPOSTSFRAGMENT", "ON DESTROY");
    }
}
