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
import android.widget.Toast;

import com.devender.whatsapp.MainActivity;
import com.devender.whatsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button mRegister, mPhone;
    private EditText mEmail, mPassword ;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private DatabaseReference currentUserDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgress = new ProgressDialog(RegisterActivity.this);
        mRegister = findViewById(R.id.register_button);
        mPhone = findViewById(R.id.register_phone);
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);

        mAuth = FirebaseAuth.getInstance();
        currentUserDB = FirebaseDatabase.getInstance().getReference();

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setTitle("Creating new Account...");
                mProgress.setMessage("Please Wait, While your account is creating....");
                mProgress.setCanceledOnTouchOutside(true);
                mProgress.show();

                final String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful()) {
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                
                                String userId = mAuth.getCurrentUser().getUid();
                                currentUserDB.child("Users").child(userId).setValue(" ");
                                currentUserDB.child("Users").child(userId).child("device_token").setValue(deviceToken);
                                currentUserDB.child("email").setValue(email);
                                
                                Toast.makeText(RegisterActivity.this, "Account is Create SuccussFully...", Toast.LENGTH_SHORT).show();

                                Intent intent= new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();

                                mProgress.dismiss();
                            }
                            else
                                {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }
        });
        mPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, PhoneRegisterActivity.class);
                startActivity(intent);
            }
        });
    }}

