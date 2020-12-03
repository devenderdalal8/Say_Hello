package com.devender.whatsapp.Registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devender.whatsapp.MainActivity;
import com.devender.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button mLogin, mRegisterWithPhone, mRegisterWithEmail;
    private EditText mEmail, mPassword;
    private TextView mForgetPassword;
    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseReference;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgress = new ProgressDialog(LoginActivity.this);
        mForgetPassword = findViewById(R.id.forget_password_link);
        mLogin = findViewById(R.id.login_button);
        mRegisterWithEmail = (Button) findViewById(R.id.login_register_with_email);
        mRegisterWithPhone = findViewById(R.id.login_register_with_phone);
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mCurrentUser != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Please fill Email and Password...", Toast.LENGTH_SHORT).show();
                } else {
                    mProgress.show();
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String mCurrentUid = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                mDatabaseReference.child(mCurrentUid).child("device_token").setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(LoginActivity.this, "Sign In Successfull", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    mProgress.dismiss();
                                                }
                                            }
                                        });
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        mRegisterWithEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mRegisterWithPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, PhoneRegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

}