package blackbox.petsnaps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "RegistrationActivity";
    private EditText usernameET, emailET, passwordET, confirmPassET;
    private Button registerBttn;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mUserDBRef;

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        firebaseAuth = FirebaseAuth.getInstance();

        usernameET = (EditText) findViewById(R.id.username_et);
        emailET = (EditText) findViewById(R.id.email_et);
        passwordET = (EditText) findViewById(R.id.password_et);
        confirmPassET = (EditText) findViewById(R.id.confirm_password_et);
        registerBttn = (Button) findViewById(R.id.registration_bttn);
        registerBttn.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
    }

    private void registerUser() {
        username = usernameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String confirmPass = confirmPassET.getText().toString().trim();

        if (username.length() == 0 || username.length() > 12) {
            usernameET.setError("Please enter a username with 1-12 characters.");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.setError("Please enter a valid email.");
            return;
        }
        if (password.length() < 6) {
            passwordET.setError("Password must be at least 6 characters.");
            return;
        }
        if (!confirmPass.equals(password)) {
            confirmPassET.setError("Passwords do not match.");
            return;
        }

        // Email and password are valid
        progressDialog.setMessage("Registering...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            onRegistrationSuccess();
                        }
                        else {
                            onRegistrationFailure();
                            Log.e(TAG, "Failed Registration", task.getException());
                        }
                    }
                });


    }

    private void onRegistrationSuccess() {
        // ENTER USER INTO DATABASE
        mUserDBRef = FirebaseDatabase.getInstance().getReference().child("Users");
        String user_id = firebaseAuth.getCurrentUser().getUid();
        final DatabaseReference newUser = mUserDBRef.child(user_id);
        newUser.child("username").setValue(username);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.registration_success_title)
                .setMessage(R.string.registration_success_message)
                .setPositiveButton(R.string.registration_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                })
                .show();
    }

    private void onRegistrationFailure() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.registration_failure_title)
                .setMessage(R.string.registration_failure_message)
                .setPositiveButton(R.string.registration_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // message confirmed, do nothing
                    }
                })
                .show();
    }

    @Override
    public void onClick(View v) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        registerUser();
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

}
