package blackbox.petsnaps;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

/**
 * Created by tricia on 7/26/17.
 */

public class CommentItemViewHolder extends RecyclerView.ViewHolder {
    private View mView;

    public CommentItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;

    }

    public void setUsername(String name) {
        TextView username = (TextView) mView.findViewById(R.id.username_tv);
        username.setText(name);
    }

    public void setMessage(String mssg) {
        TextView message = (TextView) mView.findViewById(R.id.message_tv);
        message.setText(mssg);
    }

    public void setDeleteButton(final Context context, boolean showButton, final String commentKey, final String postKey) {
        ImageView deleteIV = (ImageView) mView.findViewById(R.id.delete_iv);
        deleteIV.setVisibility(View.VISIBLE);
        deleteIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference().child("Comments").child(commentKey).removeValue();
                                decrementCommentCount(postKey);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void decrementCommentCount(final String postKey) {
        DatabaseReference numCommentsRef = FirebaseDatabase.getInstance().getReference().child("Main_Feed").child(postKey).child("numComments");
        numCommentsRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(0);
                } else {
                    mutableData.setValue(currentValue - 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                System.out.println("Transaction completed");
            }
        });
    }
}