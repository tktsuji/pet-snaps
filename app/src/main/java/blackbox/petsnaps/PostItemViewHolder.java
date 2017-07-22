package blackbox.petsnaps;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

/**
 * Created by tricia on 7/18/17.
 */

public class PostItemViewHolder extends RecyclerView.ViewHolder {
    View mView;
    ImageView heartIcon;

    DatabaseReference mLikesRef;
    FirebaseAuth mAuth;

    public PostItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        heartIcon = (ImageView) itemView.findViewById(R.id.heart_iv);

        mLikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();
        mLikesRef.keepSynced(true);
    }

    public void setTitle(String title) {
        TextView postTitle = (TextView) mView.findViewById(R.id.post_title_tv);
        postTitle.setText(title);
    }


    public void setImage(Context context, String image) {
        ImageView postImage = (ImageView) mView.findViewById(R.id.post_iv);
        Picasso.with(context).load(image).into(postImage);
    }

    public void setHeartIcon(final Context context, final String post_key) {
        final TextView numLikesTV = (TextView) mView.findViewById(R.id.num_likes_tv);
        mLikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long numLikes = dataSnapshot.child(post_key).getChildrenCount();
                numLikesTV.setText(Long.toString(numLikes));
                if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                    heartIcon.setImageResource(R.drawable.heart_liked_icon);
                    numLikesTV.setTextColor(context.getResources().getColor(R.color.colorHeart));
                }
                else {
                    heartIcon.setImageResource(R.drawable.heart_icon);
                    numLikesTV.setTextColor(context.getResources().getColor(R.color.colorPrimaryLight));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}