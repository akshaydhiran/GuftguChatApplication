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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button createAccountButton;
    private EditText userEmailEditText,userPasswordEditText;
    private TextView alreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference rootDatabaseReference;//RootRef

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth=FirebaseAuth.getInstance(); //initialize fireBase auth
        rootDatabaseReference= FirebaseDatabase.getInstance().getReference();

        InitializeFields();


        alreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }



    private void createNewAccount() {
        String email=userEmailEditText.getText().toString();
        String password=userPasswordEditText.getText().toString();

        if(TextUtils.isEmpty(email))
            Toast.makeText(this, "please enter email......", Toast.LENGTH_SHORT).show();
        if(TextUtils.isEmpty(password))
            Toast.makeText(this, "please enter password......", Toast.LENGTH_SHORT).show();
        else
        {
            loadingBar.setTitle("Creating new Account");//progressBar
            loadingBar.setMessage("please wait while we create new Account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()) //if login is successful
                            {
                                String deviceToken= FirebaseInstanceId.getInstance().getToken();

                                String currentUserId=mAuth.getCurrentUser().getUid();
                                rootDatabaseReference.child("Users").child(currentUserId).setValue("");

                                rootDatabaseReference.child("Users").child(currentUserId).child("device_token")
                                        .setValue(deviceToken);


                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message=task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }

    }

    private void InitializeFields() {

        createAccountButton=findViewById(R.id.register_button_Id);
        userEmailEditText=findViewById(R.id.register_email_Id);
        userPasswordEditText=findViewById(R.id.register_password_Id);
        alreadyHaveAccountLink=findViewById(R.id.already_have_account_link_Id);
        loadingBar=new ProgressDialog(this);

    }

    private void sendUserToLoginActivity() {
        Intent loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }


    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}