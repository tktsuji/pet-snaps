package blackbox.petsnaps;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class CreatePostActivity extends AppCompatActivity {

    private static final String APP_TAG = "PetSnaps";
    private static final String TAG = "CREATEPOSTACTIVITY";
    private ImageButton containerImgBttn;
    private EditText titleET, descrpET;
    private TextView containerTV;
    private Button submitBttn;
    private static final int GALLERY_REQUEST = 1234;
    private static final int CAMERA_REQUEST = 5678;

    private String photoFilename;

    private String mCurrentPhotoPath;
    private Uri newUri;

    private Uri imageUri = null;
    private int imageWidth, imageHeight;
    //private int imageWidth, imageHeight;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDBRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Main_Feed");
        mUserDBRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        containerImgBttn = (ImageButton) findViewById(R.id.container_img_bttn);
        titleET = (EditText) findViewById(R.id.title_tv);
        descrpET = (EditText) findViewById(R.id.descrp_tv);
        containerTV = (TextView) findViewById(R.id.container_tv);
        submitBttn = (Button) findViewById(R.id.submit_bttn);

        setUpEditTexts();

        progressDialog = new ProgressDialog(this);

        containerImgBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImage();
            }
        });

        submitBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private void setImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Get Image").setItems(R.array.image_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    photoFilename = "PetSnaps" + System.currentTimeMillis() + ".jpg";
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    newUri = getPhotoFileUri(photoFilename);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, newUri);
                    Log.d(TAG, "AFTER PUT EXTRA");
                    if (cameraIntent.resolveActivity(getPackageManager()) != null)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
                else {
                    Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, GALLERY_REQUEST);
                }
            }
        }).show();
    }

    // Returns the Uri for a photo stored on disk given the filename
    private Uri getPhotoFileUri(String filename) {
        if (isExternalStorageAvailable()) {
            File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(APP_TAG, "failed to create directory");
            }
            File file = new File(mediaStorageDir.getPath() + File.separator + filename);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                return Uri.fromFile(file);
            } else {
                return FileProvider.getUriForFile(CreatePostActivity.this, "blackbox.petsnaps.fileprovider", file);
            }
        }
        else {
            Log.v(APP_TAG, "NO EXTERNAL STORAGE AVAILABLE");
        }
        return null;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    private void submitPost() {
        progressDialog.setMessage("Submitting post...");

        final String title = titleET.getText().toString().trim();
        String descrpTemp = descrpET.getText().toString().trim();
        final String descrp = descrpTemp.replace("\n", " ");

        if (!TextUtils.isEmpty(title)  && imageUri != null) {
            progressDialog.show();
            String filename = imageUri.getLastPathSegment();
            StorageReference filepath = mStorage.child("Post_Images").child(filename);
            try {
                // COMPRESS IMAGE
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // BitmapFactory options to downsize the image
                //BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = 2;
                //InputStream is = getContentResolver().openInputStream(imageUri);
                //Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                // Compress bitmap
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 1, baos); // FOR TESTING ONLY
                /*bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);    OK QUALITY */
                /*bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); */
                byte[] data = baos.toByteArray();

                // UPLOAD IMAGE TO STORAGE
                UploadTask uploadTask = filepath.putBytes(data);

                // ENTER POST INFO INTO DATABASE
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        if (downloadUrl != null && mAuth.getCurrentUser() != null) {
                            final DatabaseReference newPost = mDatabase.push();
                            mUserDBRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    newPost.child("title").setValue(title);
                                    newPost.child("descrp").setValue(descrp);
                                    newPost.child("image").setValue(downloadUrl.toString());
                                    newPost.child("uid").setValue(mCurrentUser.getUid());
                                    newPost.child("username").setValue(dataSnapshot.child("username").getValue());
                                    newPost.child("numComments").setValue(0);

                                    // WARNING: USER'S DEVICE MAY BE SET TO WRONG TIME
                                    long reverseTime = -1 * System.currentTimeMillis();
                                    newPost.child("reverse_timestamp").setValue(reverseTime);
                                    startActivity(new Intent(CreatePostActivity.this, MainActivity.class));
                                    finish();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d("CREATEPOSTACTIVITY", "onCancelled()");
                                }
                            });
                        }
                        progressDialog.dismiss();
                    }
                });
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }
        else {
            Toast.makeText(this, "Please enter an image and title.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(APP_TAG, "ON ACTIVITY RESULT");

        if ((requestCode == GALLERY_REQUEST || requestCode == CAMERA_REQUEST) && resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST)
                imageUri = data.getData();
            else {
                Log.d(TAG, "ABOUT TO GET URI FROM CAMERA");
                imageUri = newUri;
            }

            // GET DIMENSIONS OF CONTAINER
            int imageWidth = containerImgBttn.getWidth();
            int imageHeight = containerImgBttn.getHeight();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(imageWidth, imageHeight)
                    .start(this);

            //containerImgBttn.setImageURI(imageUri);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            containerImgBttn.setImageURI(imageUri);
            containerTV.setText("");
        }
    }

    private void setUpEditTexts() {
        titleET.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
        descrpET.setFilters(new InputFilter[] {new InputFilter.LengthFilter(150)});
        final TextView titleCharCount = (TextView) findViewById(R.id.titleCharCount);
        final TextView descrpCharCount = (TextView) findViewById(R.id.descrpCharCount);
        titleET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CharSequence charSeq = Integer.toString(s.length()) + "/15";
                titleCharCount.setText(charSeq);
            }
            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        });
        descrpET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                CharSequence charSeq = Integer.toString(s.length()) + "/150";
                descrpCharCount.setText(charSeq);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // do nothing
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel")
                .setMessage("Are you sure you want to exit? Any information you have entered will be lost.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void setImageWidthHeight(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }
}
