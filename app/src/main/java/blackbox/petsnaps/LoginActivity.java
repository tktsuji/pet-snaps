package blackbox.petsnaps;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailET, passwordET;
    private TextView linkToRegisterTV;
    private Button loginBttn;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        firebaseAuth = FirebaseAuth.getInstance();

        emailET = (EditText) findViewById(R.id.login_email_et);
        passwordET = (EditText) findViewById(R.id.login_password_et);
        loginBttn = (Button) findViewById(R.id.login_bttn);
        linkToRegisterTV = (TextView) findViewById(R.id.link_to_register_tv);

        progressDialog = new ProgressDialog(this);

        linkToRegisterTV.setOnClickListener(this);
        loginBttn.setOnClickListener(this);
    }

    private void userLogin() {
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailET.setError("Please enter your email.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordET.setError("Please enter your password.");
            return;
        }

        // Email and password are not empty
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful())
                            onLoginSuccess();
                        else
                            onLoginFailure();
                    }
                });
    }

    private void onLoginSuccess() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void onLoginFailure() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.login_failure_title)
                .setMessage(R.string.login_failure_message)
                .setPositiveButton(R.string.login_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // message confirmed, do nothing
                    }
                })
                .show();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.login_bttn:
                userLogin();
                break;
            case R.id.link_to_register_tv:
                Intent i = new Intent(this, RegistrationActivity.class);
                startActivity(i);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // DO NOTHING
    }
}
