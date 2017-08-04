package blackbox.petsnaps.FilterFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import blackbox.petsnaps.PostItem;
import blackbox.petsnaps.PostItemViewHolder;
import blackbox.petsnaps.R;

public class ReptileFragment extends BaseFilterFragment {

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
                PostItemViewHolder.class, mMainFeedRef.orderByChild("tags/4").equalTo(true)
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
        Log.d("REPTILEFRAGMENT", "ON DESTROY");
    }
}