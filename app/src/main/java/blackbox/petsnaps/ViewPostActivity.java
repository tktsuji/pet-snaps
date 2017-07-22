package blackbox.petsnaps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ViewPostActivity extends AppCompatActivity {

    private String mPostKey;
    private DatabaseReference mDatabase;
    private TextView titleTV, authorTV, descrpTV;
    private ImageView mainImageIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        titleTV = (TextView) findViewById(R.id.title_tv);
        descrpTV = (TextView) findViewById(R.id.descrp_tv);
        authorTV = (TextView) findViewById(R.id.author_tv);
        mainImageIV = (ImageView) findViewById(R.id.main_image_iv);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Main_Feed");
        mPostKey = getIntent().getStringExtra("post_key");
        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String postTitle = (String) dataSnapshot.child("title").getValue();
                String postDescrp = (String) dataSnapshot.child("descrp").getValue();
                String postImage = (String) dataSnapshot.child("image").getValue();
                String postAuthor = (String) dataSnapshot.child("username").getValue();
                titleTV.setText(postTitle);
                descrpTV.setText(postDescrp);
                authorTV.setText(postAuthor);
                Picasso.with(ViewPostActivity.this).load(postImage).into(mainImageIV);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
