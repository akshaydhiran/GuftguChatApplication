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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton,phoneLoginButton;
    private EditText userEmailEditText,userPasswordEditText;
    private TextView NeedNewAccountLink,ForgetPasswordLink;

    private ProgressDialog loadingBar;


    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth=FirebaseAuth.getInstance();//using mAuth we can allow user to sign in or sign up
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();



        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });

    }

    private void allowUserToLogin() {

        String email=userEmailEditText.getText().toString();
        String password=userPasswordEditText.getText().toString();

        if(TextUtils.isEmpty(email))
            Toast.makeText(this, "please enter email......", Toast.LENGTH_SHORT).show();
        if(TextUtils.isEmpty(password))
            Toast.makeText(this, "please enter password......", Toast.LENGTH_SHORT).show();
        else {

            loadingBar.setTitle("Sign in");//progressBar
            loadingBar.setMessage("please wait....");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {

                                String currentUserId=mAuth.getCurrentUser().getUid();
                                String deviceToken= FirebaseInstanceId.getInstance().getToken();

                                usersRef.child(currentUserId).child("device_token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful())
                                                {
                                                    sendUserToMainActivity();
                                                    Toast.makeText(LoginActivity.this, "Log in successful", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }

                                            }
                                        });



                            }
                            else
                            {
                                String message=task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }


    // =======================================Intializing fields=================================
    private void InitializeFields() {

        loginButton=findViewById(R.id.login_button_Id);
        phoneLoginButton=findViewById(R.id.phone_login_button_Id);
        userEmailEditText=findViewById(R.id.login_email_Id);
        userPasswordEditText=findViewById(R.id.login_password_Id);
        NeedNewAccountLink=findViewById(R.id.need_new_account_link_Id);
        ForgetPasswordLink=findViewById(R.id.forget_password_link_id);

        loadingBar=new ProgressDialog(this);
    }





    //==================================on start of the app================================================
    // @Override
   /* protected void onStart() //whenEver the app starts
    {
        super.onStart();

        if(currentUser!=null) //it means user is authenticated(logged in)
        {
            sendUserToMainActivity();
        }
    }*/

    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void sendUserToRegisterActivity() {
        Intent registerIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }

}