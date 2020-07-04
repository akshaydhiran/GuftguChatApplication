package com.akshaysingh5.guftgu;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{

    private List<Messages> userMessageList;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessageList=userMessagesList;
    }

    public class  MessageViewHolder extends RecyclerView.ViewHolder
    {

        public TextView senderMessageText,receiverMessageText;
        public CircleImageView receiverProfileimage;
        public ImageView messageSenderPicture,messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText=itemView.findViewById(R.id.sender_message_text_id);
            receiverMessageText=itemView.findViewById(R.id.reciver_message_text_id);
            receiverProfileimage=itemView.findViewById(R.id.message_profile_image_id);

            messageReceiverPicture=itemView.findViewById(R.id.message_receiver_imageView_id);
            messageSenderPicture=itemView.findViewById(R.id.message_sender_imageView_id);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);

        mAuth=FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {
        String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages=userMessageList.get(position);

        String fromUserId=messages.getFrom();
        String fromMessageType=messages.getType();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileimage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileimage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if(fromMessageType.equals("text"))
        {


            if(fromUserId.equals(messageSenderId))
            {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                //holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage()+"\n \n"+messages.getTime()+" - "+messages.getDate());
            }
            else
            {


                holder.receiverProfileimage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                //holder.senderMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage()+"\n \n"+messages.getTime()+" - "+messages.getDate());
            }
        }

        else if(fromMessageType.equals("image"))
        {
            if(fromUserId.equals(messageSenderId))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
            }
            else
            {
                holder.receiverProfileimage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
            }
        }
        else if(fromMessageType.equals("docx")|| fromMessageType.equals("pdf"))
        {
            if(fromUserId.equals(messageSenderId))
            {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/samvad-fdcd9.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=f67cae55-cfbb-477f-9737-f2503ea59258")
                        .into(holder.messageSenderPicture);


            }
            else
            {
                holder.receiverProfileimage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/samvad-fdcd9.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=f67cae55-cfbb-477f-9737-f2503ea59258")
                        .into(holder.messageReceiverPicture);



            }
        }



        if(fromUserId.equals(messageSenderId))   //for sender activity
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {

                    if(userMessageList.get(position).getType().equals("pdf")  || userMessageList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and view this document",
                                        "cancel",
                                        "Delete for everyone"
                                };
                        AlertDialog.Builder builder =new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");


                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which==0)
                                {
                                    deleteSentMessages(position,holder);

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class); //we will send user to main Activity after deleting the image
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else  if(which==1)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                else if(which==3)
                                {
                                    deleteMessageForEveryOne(position,holder);

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class); //we will send user to main Activity after deleting the image
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }

                    else if(userMessageList.get(position).getType().equals("text")  ) //if  text message is clicked
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "cancel",
                                        "Delete for everyone"
                                };
                        AlertDialog.Builder builder =new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");


                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which==0)
                                {
                                    deleteSentMessages(position,holder);

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class); //we will send user to main Activity after deleting the image
                                    holder.itemView.getContext().startActivity(intent);

                                }

                                else if(which==2)
                                {deleteMessageForEveryOne(position, holder);

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class); //we will send user to main Activity after deleting the image
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }

                    else if(userMessageList.get(position).getType().equals("image") )
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View this Image",
                                        "cancel",
                                        "Delete for everyone"
                                };
                        AlertDialog.Builder builder =new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");


                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which==0)
                                {
                                    deleteSentMessages(position, holder);

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class); //we will send user to main Activity after deleting the image
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else  if(which==1)
                                {
                                    Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                else if(which==3)
                                {
                                    deleteMessageForEveryOne(position, holder);
                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class); //we will send user to main Activity after deleting the image
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }

                }
            });
        }

        else
        {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {

                    if(userMessageList.get(position).getType().equals("pdf")  || userMessageList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and view this document",
                                        "cancel"

                                };
                        AlertDialog.Builder builder =new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");


                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which==0)
                                {
                                    deleteReceivedMessages(position, holder);

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class); //we will send user to main Activity after deleting the image
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else  if(which==1)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        builder.show();
                    }

                    else if(userMessageList.get(position).getType().equals("text")  ) //if  text message is clicked
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "cancel"

                                };
                        AlertDialog.Builder builder =new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");


                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which==0)
                                {
                                    deleteReceivedMessages(position, holder);

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class); //we will send user to main Activity after deleting the image
                                    holder.itemView.getContext().startActivity(intent);
                                }



                            }
                        });
                        builder.show();
                    }

                    else if(userMessageList.get(position).getType().equals("image") )
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View this Image",
                                        "cancel"

                                };
                        AlertDialog.Builder builder =new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message ?");


                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which==0)
                                {
                                    deleteReceivedMessages(position, holder);

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class); //we will send user to main Activity after deleting the image
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else  if(which==1)
                                {
                                    Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }



                            }
                        });
                        builder.show();
                    }

                }
            });


        }
    }

    @Override
    public int getItemCount()
    {
        return  userMessageList.size();
    }



    private void deleteSentMessages(final int position,final MessageViewHolder holder)
    {

        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())  //sender
                .child(userMessageList.get(position).getTo())   //receiver
                .child(userMessageList.get(position).getMessageID())   // message key
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void deleteReceivedMessages(final int position,final MessageViewHolder holder)
    {

        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())  //receiver
                .child(userMessageList.get(position).getFrom())   //sender
                .child(userMessageList.get(position).getMessageID())   // message key
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deleteMessageForEveryOne(final int position,final MessageViewHolder holder)
    {

        final DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())  //sender
                .child(userMessageList.get(position).getTo())   //receiver
                .child(userMessageList.get(position).getMessageID())   // message key
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {

                    rootRef.child("Messages")
                            .child(userMessageList.get(position).getTo())  //receiver
                            .child(userMessageList.get(position).getFrom())   //sender
                            .child(userMessageList.get(position).getMessageID())   // message key
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




}
