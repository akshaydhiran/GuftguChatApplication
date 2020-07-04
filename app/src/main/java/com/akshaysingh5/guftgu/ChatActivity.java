package com.akshaysingh5.guftgu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{


    private  String messageReceiverId,messageReceiverName,messageReceiverImage ,  messageSenderId;
    private String saveCurrentTime,saveCurrentdate;


    private TextView userNameTextView,userLastSceneTextView;
    private CircleImageView userImage;
    private RecyclerView userMessageslist;

    private ImageButton sendMessageButton,sendFilesButton;
    private EditText messageInputText;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private  String checker="",myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;
    private Toolbar chatToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();


        messageReceiverId=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage=getIntent().getExtras().get("visit_user_image").toString();


        InitializeControllers();





        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages=dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();

                        userMessageslist.smoothScrollToPosition(userMessageslist.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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


        userNameTextView.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
        displayLastScene();


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CharSequence options[]=new CharSequence[]
                        {
                                "Images",
                                "PDF FIles",
                                "Ms Word Filees"
                        };

                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(which==0)
                        {
                            checker="image";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent,"Select Image"),438);
                        }
                        if(which==1)
                        {
                            checker="pdf";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(Intent.createChooser(intent,"Select PDF File"),438);
                        }
                        if(which==2)
                        {
                            checker="docx";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(Intent.createChooser(intent,"Select MS WORD FIle"),438);
                        }


                    }
                });
                builder.show();
            }
        });


    }

    private void InitializeControllers() {

        chatToolBar=findViewById(R.id.chat_toolBar_Id);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView= layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);


        userImage=findViewById(R.id.custom_profile_image_Id);
        userNameTextView=findViewById(R.id.custom_profile_name_Id);
        userLastSceneTextView=findViewById(R.id.custom_user_last_scene_Id);

        sendMessageButton=findViewById(R.id.send_message_chat_btn_Id);
        messageInputText=findViewById(R.id.input_chat_Message_Id);
        sendFilesButton=findViewById(R.id.send_files_btn_Id);


        messageAdapter=new MessageAdapter(messagesList);
        userMessageslist=findViewById(R.id.private_messages_users_list_id);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessageslist.setLayoutManager(linearLayoutManager);
        userMessageslist.setAdapter(messageAdapter);

        loadingBar=new ProgressDialog(this);


        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentdate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("please wait while we send your file.....");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            fileUri=data.getData();

            if(!checker.equals("image")) //if user selects doc or pdf instead of image
            {

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef="Messages/"+messageSenderId+"/"+messageReceiverId;
                final String messageReceiverRef="Messages/"+messageReceiverId+"/"+messageSenderId;


                DatabaseReference userMessageKeyRef=rootRef.child("Messages").child(messageSenderId)
                        .child(messageReceiverId).push() ;

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath=storageReference.child(messagePushId+"."+"checker");

               /* filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {

                            Map messageTextBody=new HashMap();
                            messageTextBody.put("message",task.getResult().getMetadata().getReference().getDownloadUrl().toString());
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", messageReceiverId);
                            messageTextBody.put("messageID", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentdate);


                            Map messageBodyDetails=new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

                            rootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, "Error!!..."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double P=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int)P+" % Uploading....");
                    }
                });*/



                uploadTask=filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", messageReceiverId);
                            messageTextBody.put("messageID", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentdate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, "Error!!..."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                ;





            }
            else if(checker.equals("image"))
            {


                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef="Messages/"+messageSenderId+"/"+messageReceiverId;
                final String messageReceiverRef="Messages/"+messageReceiverId+"/"+messageSenderId;


                DatabaseReference userMessageKeyRef=rootRef.child("Messages").child(messageSenderId)
                        .child(messageReceiverId).push() ;

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath=storageReference.child(messagePushId+"."+"jpg");

                uploadTask=filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful())
                        {
                            Uri downloadUrl=task.getResult();
                            myUrl=downloadUrl.toString();

                            Map messageTextBody=new HashMap();
                            messageTextBody.put("message",myUrl);
                            messageTextBody.put("name",fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", messageReceiverId);
                            messageTextBody.put("messageID", messagePushId);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentdate);


                            Map messageBodyDetails=new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {

                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message sent successfully: ", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });

            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing selected.....  Errrorrr!!!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private  void displayLastScene()
    {
        rootRef.child("Users").child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        if(dataSnapshot.exists())
                        {
                            if(dataSnapshot.hasChild("userState"))
                            {

                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online"))
                                {
                                    userLastSceneTextView.setText("online");
                                }
                                else if(state.equals("offline"))
                                {
                                    userLastSceneTextView.setText("Last Scene: "+date +" "+time );
                                }
                            }
                            else
                            {
                                userLastSceneTextView.setText("offline");

                            }


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /* @Override
     protected void onStart() {
         super.onStart();


         rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                 .addChildEventListener(new ChildEventListener() {
                     @Override
                     public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                     {
                         Messages messages=dataSnapshot.getValue(Messages.class);
                         messagesList.add(messages);
                         messageAdapter.notifyDataSetChanged();

                         userMessageslist.smoothScrollToPosition(userMessageslist.getAdapter().getItemCount());
                     }

                     @Override
                     public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
 */
    private void sendMessage()
    {
        String messageText=messageInputText.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "please write your message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef="Messages/"+messageSenderId+"/"+messageReceiverId;
            String messageReceiverRef="Messages/"+messageReceiverId+"/"+messageSenderId;


            DatabaseReference userMessageKeyRef=rootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push() ;

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);
            messageTextBody.put("to", messageReceiverId);
            messageTextBody.put("messageID", messagePushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentdate);


            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message sent successfully: ", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });
        }
    }
}