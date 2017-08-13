package blackbox.petsnaps.FilterFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import blackbox.petsnaps.PostItem;
import blackbox.petsnaps.PostItemViewHolder;
import blackbox.petsnaps.R;

public class TopPostsFragment extends BaseFilterFragment {

    FirebaseRecyclerAdapter<PostItem, PostItemViewHolder> firebaseRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("TOPPOSTSFRAGMENT", "ON START");
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostItem, PostItemViewHolder>(
                PostItem.class,
                R.layout.post_item,
                PostItemViewHolder.class, mMainFeedRef.orderByChild("numLikes").limitToLast(64)
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
        Log.d("TOPPOSTSFRAGMENT", "ON DESTROY");
    }
}
