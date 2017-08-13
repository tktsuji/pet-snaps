package blackbox.petsnaps.FilterFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import blackbox.petsnaps.PostItem;
import blackbox.petsnaps.PostItemViewHolder;
import blackbox.petsnaps.R;

public class RodentFragment extends BaseFilterFragment {

    private FirebaseRecyclerAdapter<Long, PostItemViewHolder> firebaseRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("RODENTSFRAGMENT", "ON START");
        DatabaseReference rodentRef = FirebaseDatabase.getInstance().getReference().child("Rodent_Posts");
        final DatabaseReference mPostKeys = FirebaseDatabase.getInstance().getReference().child("Post_Keys");
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Long, PostItemViewHolder>(
                Long.class,
                R.layout.post_item,
                PostItemViewHolder.class, rodentRef.orderByValue().limitToLast(64)) {
            @Override
            protected void populateViewHolder(PostItemViewHolder viewHolder, Long model, int position) {
                final String post_key = getRef(position).getKey();
                final PostItemViewHolder vH = viewHolder;
                final int p = position;
                mPostKeys.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.getKey().equals(post_key))
                                superPopViewHolder(vH, post_key);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
        Log.d("RODENTFRAGMENT", "ON DESTROY");
    }
}