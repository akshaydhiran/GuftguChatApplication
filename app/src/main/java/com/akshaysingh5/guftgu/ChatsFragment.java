package com.akshaysingh5.guftgu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment
{

    private View privateChatsView;
    private RecyclerView chatList;

    private DatabaseReference chatsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;



    public ChatsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);

        chatList=privateChatsView.findViewById(R.id.chats_list_id);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));


        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        chatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");



        return privateChatsView;

    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatsRef,Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        final String usersId=getRef(position).getKey();
                        final String[] userProfileImage = {"default_image"};

                        UsersRef.child(usersId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {

                                if(dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild("image"))
                                    {

                                        userProfileImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(userProfileImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage); //as profile image exists

                                    }

                                    final String profileName = dataSnapshot.child("name").getValue().toString();
                                    String profileStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(profileName);


                                    if(dataSnapshot.child("userState").hasChild("state"))
                                    {

                                        String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                        if(state.equals("online"))
                                        {
                                            holder.userStatus.setText("online");
                                        }
                                        else if(state.equals("offline"))
                                        {
                                            holder.userStatus.setText("Last Scene: "+date +" "+time );
                                        }
                                    }
                                    else
                                    {
                                        holder.userStatus.setText("offline");

                                    }



                                    holder.itemView.setOnClickListener(new View.OnClickListener() {  //when we click on the chat
                                        @Override
                                        public void onClick(View v)
                                        {

                                            Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id",usersId);
                                            chatIntent.putExtra("visit_user_name",profileName);
                                            chatIntent.putExtra("visit_user_image", userProfileImage[0]);
                                            startActivity(chatIntent);

                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);

                        ChatsViewHolder viewHolder=new ChatsViewHolder(view);
                        return viewHolder;
                    }
                };

        chatList.setAdapter(adapter);
        adapter.startListening();
    }


    public  static  class ChatsViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name_id);
            userStatus=itemView.findViewById(R.id.user_status_textView_id);
            profileImage=itemView.findViewById(R.id.user_profile_image_id);

        }
    }
}