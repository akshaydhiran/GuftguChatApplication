package com.akshaysingh5.guftgu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthMultiFactorException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment
{
    private View contactsView;
    private RecyclerView myContactsList;

    private DatabaseReference ContactRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView= inflater.inflate(R.layout.fragment_contacts, container, false);



        myContactsList=contactsView.findViewById(R.id.contacts_recycler_list_Id);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));


        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        ContactRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");


        return  contactsView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull Contacts model)
            {

                String usersIDs=getRef(position).getKey();

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        if(dataSnapshot.exists())
                        {
                            if(dataSnapshot.child("userState").hasChild("state"))
                            {

                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online"))
                                {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline"))
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else
                            {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);

                            }



                            if(dataSnapshot.hasChild("image"))
                            {

                                String userProfileImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                Picasso.get().load(userProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage); //as profile image exists
                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }
                            else
                            {


                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {

                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);

                ContactViewHolder viewHolder=new ContactViewHolder(view);
                return viewHolder;
            }
        };


        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }


    public  static  class ContactViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name_id);
            userStatus=itemView.findViewById(R.id.user_status_textView_id);
            profileImage=itemView.findViewById(R.id.user_profile_image_id);
            onlineIcon=itemView.findViewById(R.id.user_online_status_icon_id);

        }
    }
}