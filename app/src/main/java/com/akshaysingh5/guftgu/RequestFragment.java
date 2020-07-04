package com.akshaysingh5.guftgu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment
{
    private View RequestsFragmentView;
    private RecyclerView myRequestLists;

    private DatabaseReference chatRequestsRef,UserRef,ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView=inflater.inflate(R.layout.fragment_request, container, false);


        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestsRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef =FirebaseDatabase.getInstance().getReference().child("Contacts");



        myRequestLists=RequestsFragmentView.findViewById(R.id.chat_request_list_Id);
        myRequestLists.setLayoutManager(new LinearLayoutManager(getContext()));




        return RequestsFragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRequestsRef.child(currentUserId),Contacts.class)
                        .build();
        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model)
                    {
                        holder.itemView.findViewById(R.id.request_accept_button_id).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_button_id).setVisibility(View.VISIBLE);



                        final String list_user_id=getRef(position).getKey();

                        DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    String type=dataSnapshot.getValue().toString();
                                    if(type.equals("received")) //if the user has received request
                                    {
                                        UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {


                                                if(dataSnapshot.hasChild("image"))
                                                {

                                                    final String requestProfileImage=dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImageView);
                                                }

                                                final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("Wants to connect with you");



                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                        CharSequence options[]=new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder  builder =new AlertDialog.Builder((getContext()));
                                                        builder.setTitle(requestUserName + "Chat Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {

                                                                if(which==0)   //user clicks accept button
                                                                {
                                                                    ContactsRef.child(currentUserId).child(list_user_id).child("Contact")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {

                                                                                ContactsRef.child(list_user_id).child(currentUserId).child("Contact")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            chatRequestsRef.child(currentUserId).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                                        {

                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                chatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                                            {

                                                                                                                                if(task.isSuccessful())
                                                                                                                                {
                                                                                                                                    Toast.makeText(getContext(), "New Contact added", Toast.LENGTH_SHORT).show();

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
                                                                        }
                                                                    });
                                                                }
                                                                if(which==1) //if you click on cancel button
                                                                {

                                                                    chatRequestsRef.child(currentUserId).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {

                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        chatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {

                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "Contact deleted", Toast.LENGTH_SHORT).show();

                                                                                                        }

                                                                                                    }
                                                                                                });
                                                                                    }

                                                                                }
                                                                            });

                                                                }
                                                            }
                                                        });

                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    else if(type.equals("sent")) //==========user has sent request
                                    {
                                        Button request_sent_button=holder.itemView.findViewById(R.id.request_accept_button_id);
                                        request_sent_button.setText("Req sent");

                                        holder.itemView.findViewById(R.id.request_cancel_button_id).setVisibility(View.INVISIBLE);

                                        UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {


                                                if(dataSnapshot.hasChild("image"))
                                                {

                                                    final String requestProfileImage=dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImageView);
                                                }

                                                final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus=dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("you sent reuest to: "+requestUserName);



                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                        CharSequence options[]=new CharSequence[]
                                                                {

                                                                        "Cancel Chat Request"
                                                                };
                                                        AlertDialog.Builder  builder =new AlertDialog.Builder((getContext()));
                                                        builder.setTitle("Alredy sent request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which)
                                                            {


                                                                if(which==0) //if you click on cancel button
                                                                {

                                                                    chatRequestsRef.child(currentUserId).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {

                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        chatRequestsRef.child(list_user_id).child(currentUserId)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                    {

                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "you have cancelled the chat request", Toast.LENGTH_SHORT).show();

                                                                                                        }

                                                                                                    }
                                                                                                });
                                                                                    }

                                                                                }
                                                                            });

                                                                }
                                                            }
                                                        });

                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        RequestViewHolder holder=new RequestViewHolder(view);
                        return holder;

                    }
                };
        myRequestLists.setAdapter(adapter);
        adapter.startListening();

    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImageView;
        Button acceptButton,cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);


            userName=itemView.findViewById(R.id.user_profile_name_id);
            userStatus=itemView.findViewById(R.id.user_status_textView_id);
            profileImageView=itemView.findViewById(R.id.user_profile_image_id);
            acceptButton=itemView.findViewById(R.id.request_accept_button_id);
            cancelButton=itemView.findViewById(R.id.request_cancel_button_id);


        }
    }
}