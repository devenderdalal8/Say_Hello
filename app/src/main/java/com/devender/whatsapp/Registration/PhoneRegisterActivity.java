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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneRegisterActivity extends AppCompatActivity {
    private Button mSendVerificationCodeButton , mVerifyCodeButton;
    private EditText mPhoneNumber,mVerifyNumber;

    private ProgressDialog mProgress;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_register);


        mPhoneNumber = findViewById(R.id.phone_number);
        mVerifyNumber = findViewById(R.id.phone_Verify);
        mSendVerificationCodeButton = findViewById(R.id.phone_verification_code_button);
        mVerifyCodeButton = findViewById(R.id.phone_Verify_button);
        mProgress = new ProgressDialog(PhoneRegisterActivity.this);

        mAuth = FirebaseAuth.getInstance();

        mSendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = mPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneRegisterActivity.this, "Phone Number is required... ", Toast.LENGTH_SHORT).show();
                } else
                    {
                    mProgress.setTitle("Phone Verification");
                    mProgress.setMessage("Please Wait, while we are authenticating your phone...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                    
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            120,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneRegisterActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                    
                }

            }
        });

        mVerifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendVerificationCodeButton.setVisibility(View.INVISIBLE);
                mPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = mVerifyNumber.getText().toString();

                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneRegisterActivity.this, "Please Write Verification Code...", Toast.LENGTH_SHORT).show();
                } else {
                    mProgress.setTitle("Verification Code");
                    mProgress.setMessage("Please Wait, while we are verifying verification your code...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
                
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
               signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {
                mProgress.dismiss();
                Toast.makeText(PhoneRegisterActivity.this, "Invalid Phone Number , Please type Right Phone Number with Country Code...", Toast.LENGTH_SHORT).show();

                mSendVerificationCodeButton.setVisibility(View.VISIBLE);
                mPhoneNumber.setVisibility(View.VISIBLE);
                mVerifyNumber.setVisibility(View.INVISIBLE);
                mVerifyCodeButton.setVisibility(View.INVISIBLE);

            }
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token)
            {
                mVerificationId = verificationId;
                mResendToken = token;
                mProgress.dismiss();
                Toast.makeText(PhoneRegisterActivity.this, "Code has been sent, Please Check and Verify ", Toast.LENGTH_SHORT).show();

                mSendVerificationCodeButton.setVisibility(View.INVISIBLE);
                mPhoneNumber.setVisibility(View.INVISIBLE);
                mVerifyNumber.setVisibility(View.VISIBLE);
                mVerifyCodeButton.setVisibility(View.VISIBLE);
            }

        };
    }

    
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
     {
         
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            mProgress.dismiss();
                            Intent intent = new Intent(PhoneRegisterActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        }
                        else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(PhoneRegisterActivity.this, " Error : " + message, Toast.LENGTH_SHORT).show();
                            }
                    }
                });
    }
}