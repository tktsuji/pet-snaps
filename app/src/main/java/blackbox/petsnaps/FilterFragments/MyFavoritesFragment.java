package blackbox.petsnaps.FilterFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import blackbox.petsnaps.PostItemViewHolder;
import blackbox.petsnaps.R;


public class MyFavoritesFragment extends BaseFilterFragment {
    String user_id;
    private FirebaseRecyclerAdapter<String, PostItemViewHolder> firebaseRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        user_id = this.getArguments().getString("uid");
        //user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        System.out.println(user_id);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("FAVSFRAGMENT", "ON START");
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<String, PostItemViewHolder>(
                String.class,
                R.layout.post_item,
                PostItemViewHolder.class, mUsersRef.child(user_id).child("Favorites").orderByValue().equalTo("true").limitToLast(64)
                /*mLikesRef.orderByChild(user_id).equalTo("true")*/
        ) {
            @Override
            protected void populateViewHolder(PostItemViewHolder viewHolder, String model, int position) {
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
        Log.d("MYFAVORITESFRAG", "ON DESTROY");
    }
}