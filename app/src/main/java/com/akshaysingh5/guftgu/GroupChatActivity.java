package com.akshaysingh5.guftgu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, groupNameRef ,groupMessageKeyRef;

    private String currentGroupName,currentUserId,currentUsername,currentDate,currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show();



        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid(); //by using this Id we can retreive any info from database
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);



        initializeFIelds();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                saveMessageInfoToDatabase();
                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });
    }










    // ==============================on start of the app we want to show the previous chats in the group ====================//

    @Override
    protected void onStart() {

        super.onStart();
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void initializeFIelds()
    {

        mToolBar=findViewById(R.id.group_chat_bar_layout_id);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton=findViewById(R.id.send_message_Imagebutton);
        userMessageInput=findViewById(R.id.input_group_Message_Id);
        displayTextMessage=findViewById(R.id.group_chat_text_display_Id);
        mScrollView=findViewById(R.id.my_scrollView_Id);
    }


    private void getUserInfo() {

        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    currentUsername=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void saveMessageInfoToDatabase() {

        String message = userMessageInput.getText().toString();
        String messageKey=groupNameRef.push().getKey();   //a key will made using this method;

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "please write message first.....", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate =Calendar.getInstance();
            java.text.SimpleDateFormat currentDateFormat=new java.text.SimpleDateFormat("MMM dd, yyyy");
            currentDate =currentDateFormat.format(calForDate.getTime());

            Calendar calForTime =Calendar.getInstance();
            java.text.SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime =currentTimeFormat.format(calForTime.getTime());


            HashMap<String ,Object> groupMessagekey=new HashMap<>();
            groupNameRef.updateChildren(groupMessagekey);

            groupMessageKeyRef=groupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap=new HashMap<>();
            messageInfoMap.put("name",currentUsername);          //updating data in firebase
            messageInfoMap.put("message",message);         //updating data in firebase
            messageInfoMap.put("date",currentDate);         //updating data in firebase
            messageInfoMap.put("time",currentTime);         //updating data in firebase
            groupMessageKeyRef.updateChildren(messageInfoMap);

        }

    }


    private void DisplayMessages(DataSnapshot dataSnapshot) {

        Iterator iterator=dataSnapshot.getChildren().iterator();

        while(iterator.hasNext())
        {
            //Toast.makeText(this, "please Wait...", Toast.LENGTH_SHORT).show();
            String chatDate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=((DataSnapshot)iterator.next()).getValue().toString();
            String chatTime=((DataSnapshot)iterator.next()).getValue().toString();

            String dateTime=chatTime+"     "+chatDate;
            StyleSpan boldSpan=new StyleSpan(Typeface.BOLD);
            SpannableString ss=new SpannableString(chatName);
            ss.setSpan(boldSpan,0,chatName.length(), Spanned.SPAN_INTERMEDIATE);

            SpannableString ss1=new SpannableString(chatMessage);
            StyleSpan italicSpan=new StyleSpan(Typeface.ITALIC);
            ss1.setSpan(italicSpan,0,chatMessage.length(), Spanned.SPAN_INTERMEDIATE);

            SpannableString ss2=new SpannableString(dateTime);
            StyleSpan boldItalicSpan=new StyleSpan(Typeface.BOLD_ITALIC);
            ss2.setSpan(boldItalicSpan,0,dateTime.length(), Spanned.SPAN_INTERMEDIATE);


            //displayTextMessage.setTextColor(Color.BLUE);
            displayTextMessage.append(ss);
            displayTextMessage.append(": \n");
            displayTextMessage.append(ss1);
            displayTextMessage.append(" \n");
            displayTextMessage.append(ss2);
            displayTextMessage.append("\n\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }
}