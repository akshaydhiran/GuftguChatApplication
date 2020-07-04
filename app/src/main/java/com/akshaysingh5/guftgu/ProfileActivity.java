package com.akshaysingh5.guftgu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserId ,senderUserId, current_State;

    private CircleImageView userProfileImageView;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton,declineMessageRequestButton;

    private DatabaseReference userRef,chatRequestRef,contactsRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth=FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        senderUserId=mAuth.getCurrentUser().getUid();


        //Toast.makeText(this, "User Id: "+receiveUserId, Toast.LENGTH_SHORT).show();


        userProfileImageView=findViewById(R.id.visit_profile_image_Id);
        userProfileStatus=findViewById(R.id.visit_profile_status_id);
        userProfileName = findViewById(R.id.visit_user_name_id);
        sendMessageRequestButton=findViewById(R.id.send_message_request_button_ID);
        declineMessageRequestButton=findViewById(R.id.decline_message_request_button_ID);
        current_State="new";


        retreiveUserInfo();
    }

    private void retreiveUserInfo()
    {

        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))    //if profile image exists
                {
                    String userImage=dataSnapshot.child("image").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImageView); //as profile image exists
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);


                    manageChatRequest();

                }
                else  //if profile image does not exist
                {
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void manageChatRequest()
    {
        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type =dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent"))
                            {
                                current_State="request_sent";
                                sendMessageRequestButton.setText("Cancel request");
                            }
                            else if(request_type.equals("received"))
                            {
                                current_State="request_received";
                                sendMessageRequestButton.setText("Accept Request");

                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);

                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        cancelChatRequest();
                                    }
                                });

                            }

                        }
                        else
                        {
                            contactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                            if(dataSnapshot.hasChild(receiverUserId))
                                            {
                                                current_State="friends";
                                                sendMessageRequestButton.setText("Remove Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        if(!senderUserId.equals(receiverUserId))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);

                    if(current_State.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if(current_State.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if(current_State.equals("request_received"))
                    {
                        acceptChatRequest();
                    }
                    if(current_State.equals("friends"))
                    {
                        removeSpecificContact();
                    }

                }
            });
        }
        else
        {
            sendMessageRequestButton.setVisibility(View.GONE);
        }
    }



    private void sendChatRequest()
    {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {


                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {

                                            if(task.isSuccessful()) {

                                                HashMap<String,String> chatNotificationMap=new HashMap<>();
                                                chatNotificationMap.put("from",senderUserId);
                                                chatNotificationMap.put("type","request");

                                                notificationRef.child(receiverUserId).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful())
                                                                {

                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    current_State = "request_send";
                                                                    sendMessageRequestButton.setText("Cancel Request");

                                                                }

                                                            }
                                                        });


                                            }

                                        }
                                    });
                        }

                    }
                });
    }



    private void cancelChatRequest()
    {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {

                        if(task.isSuccessful())
                        {

                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {

                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                current_State="new";
                                                sendMessageRequestButton.setText("Send Request");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });

                        }

                    }
                });
    }






    private void acceptChatRequest()
    {
        contactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {

                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {


                                                chatRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {



                                                        if(task.isSuccessful())
                                                        {

                                                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {

                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    current_State="friends";
                                                                    sendMessageRequestButton.setText("Remove Contact");

                                                                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                    declineMessageRequestButton.setEnabled(false);

                                                                }
                                                            });

                                                        }





                                                    }
                                                });


                                            }
                                        }
                                    });

                        }
                    }
                });
    }



    private void removeSpecificContact()
    {

        contactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {

                        if(task.isSuccessful())
                        {

                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {

                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                current_State="new";
                                                sendMessageRequestButton.setText("Send Request");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });

                        }

                    }
                });

    }

}