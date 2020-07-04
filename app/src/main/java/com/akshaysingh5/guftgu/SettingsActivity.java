package com.akshaysingh5.guftgu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettingsButton;
    private EditText userNameEditText,userStatusEditText;
    private CircleImageView userProfileImage;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private StorageReference userProfileImageReference;

    private static final int galleryPic=1;
    private ProgressDialog loadingBar;

    private Toolbar settingsToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        userProfileImageReference= FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        userNameEditText.setVisibility(View.INVISIBLE);

        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        retreiveUserInformation(); //this will set the value of username and status in settings

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent =new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPic);
            }
        });
    }




    private void InitializeFields() {
        updateAccountSettingsButton=findViewById(R.id.update_settings_button_Id);
        userNameEditText=findViewById(R.id.set_user_name_Id);
        userProfileImage=(CircleImageView)findViewById(R.id.profile_image_Id);
        userStatusEditText=findViewById(R.id.set_profile_status_Id);
        loadingBar=new ProgressDialog(this);

        settingsToolBar=findViewById(R.id.settings_toolBar_Id);
        setSupportActionBar(settingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPic && resultCode==RESULT_OK && data!=null)
        {

            //Toast.makeText(this, "Inside onActivity", Toast.LENGTH_SHORT).show();
            Uri ImageUri=data.getData();
            //Picasso.get().load(ImageUri).into(userProfileImage);

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("please wait your profile image is updating");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri=result.getUri();

                final StorageReference filePath=userProfileImageReference.child(currentUserId+resultUri.getLastPathSegment());

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                RootRef.child("Users").child(currentUserId).child("image").setValue(String.valueOf(uri)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SettingsActivity.this, "image stored in realtime database ", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                });
                            }
                        });
                    }
                });

                /*filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                        if(task.isSuccessful()) //if image get stored in firebase storage successfully
                        {
                            Toast.makeText(SettingsActivity.this, "profileImage uploaded successfully", Toast.LENGTH_SHORT).show(); //profile uploaded in firebase storage


                            // storing the data in the database
                           final String downloadUrl=task.getResult().getStorage().getDownloadUrl().toString();      ///// Error!! may be in this line

                           RootRef.child("Users").child(currentUserId).child("image")
                                   .setValue(downloadUrl)
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task)
                                       {
                                            if(task.isSuccessful()) //if image stores in firebase database successfully
                                            {
                                                Toast.makeText(SettingsActivity.this, "image saved in database", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message=task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }


                                       }
                                   });

                        }
                        else
                        {
                            String message=task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
*/            }
        }


    }

    private void updateSettings()
    {
        String userName=userNameEditText.getText().toString();
        String userStatus=userStatusEditText.getText().toString();

        if(TextUtils.isEmpty(userName))
        {
            Toast.makeText(this, "set User Name.....", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(userStatus))
        {
            Toast.makeText(this, "set User status.....", Toast.LENGTH_SHORT).show();
        }
        else //else save data in firebase
        {
            HashMap<String,Object>  profileMap=new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",userName);
            profileMap.put("status",userStatus);

            RootRef.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {


                    if(task.isSuccessful()) //if profile updated is successful
                    {
                        Toast.makeText(SettingsActivity.this, "profile updated successfully", Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();
                    }
                    else
                    {
                        String message=task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }


    private void retreiveUserInformation() { //to show usename,image

        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))&& (dataSnapshot.hasChild("image")))  //i.e if user has created profile and data(username,status,image) all exists under "Users"
                        {
                            String retreiveUserName=dataSnapshot.child("name").getValue().toString();
                            String retreiveUserStatus=dataSnapshot.child("status").getValue().toString();
                            String retreiveProfileImage=dataSnapshot.child("image").getValue().toString();

                            userNameEditText.setText(retreiveUserName);
                            userStatusEditText.setText(retreiveUserStatus);
                            Picasso.get().load(retreiveProfileImage).into(userProfileImage);

                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))  //if the user has set his username and status but not profile image
                        {
                            String retreiveUserName=dataSnapshot.child("name").getValue().toString();
                            String retreiveUserStatus=dataSnapshot.child("status").getValue().toString();

                            userNameEditText.setText(retreiveUserName);
                            userStatusEditText.setText(retreiveUserStatus);


                        }
                        else
                        {
                            userNameEditText.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "please update your profile information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}