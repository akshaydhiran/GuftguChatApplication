package com.akshaysingh5.guftgu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton,verifyButton;
    private EditText inputPhoneNoEditText,inputVerificationCodeEditText;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        loadingBar=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();

        sendVerificationCodeButton=findViewById(R.id.send_verCode_button_Id);
        verifyButton=findViewById(R.id.verify_button_Id);
        inputPhoneNoEditText=findViewById(R.id.phone_number_input_EditText_Id);
        inputVerificationCodeEditText=findViewById(R.id.verification_code_input_EditText_Id);


        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String phoneNumber=inputPhoneNoEditText.getText().toString();

                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "please enter your phone no.", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait while we are authenticating your phone");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNoEditText.setVisibility(View.INVISIBLE);

                String verificationCode =inputVerificationCodeEditText.getText().toString();

                if(TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "enter the code", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("please wait while we are verifying your code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }


            }
        });


        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this, "Error!!!..."+e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                inputPhoneNoEditText.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                inputVerificationCodeEditText.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                loadingBar.dismiss();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "code has been sent", Toast.LENGTH_SHORT).show();


                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNoEditText.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCodeEditText.setVisibility(View.VISIBLE);

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
                        if (task.isSuccessful()) // user provided correct code
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congo!! you logged in successfully", Toast.LENGTH_SHORT).show();
                            sendUsertoMainActivity();
                        }
                        else
                        {
                            String message=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error!!: "+message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void sendUsertoMainActivity()
    {
        Intent mainIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        //mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}