package com.khan.chitchat;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.util.Date;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private EditText mEditComment;
    private RecyclerView mRecyclerView;
    private ImageButton btnPostComment;
    private String PostKey,randomKey,currentDate,currentUser;
    private DatabaseReference UserRef,PostRef;
    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        PostKey  = getIntent().getExtras().get("PostKey").toString();
        //......................................................................
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comments");
        //......................................................................

        currentUser = mAuth.getCurrentUser().getUid();

        mEditComment = findViewById(R.id.edit_comment);

        mRecyclerView = findViewById(R.id.comments_list);
        mRecyclerView.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        btnPostComment = (ImageButton) findViewById(R.id.btn_post_comment);
        btnPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserRef.child(currentUser).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            validateData(userName);
                            mEditComment.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Comments> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(PostRef,Comments.class)
                .build();

        adapter =
                new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(firebaseRecyclerOptions) {
            @Override
            public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_comments_layout, parent, false);

                return new CommentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(CommentsViewHolder holder, int position, Comments model) {
                // Bind the Chat object to the ChatHolder
                // ...
                holder.setUsername(model.getUsername());
                holder.setDate(model.getDate());
                holder.setComment(model.getComment());
            }
        };
        mRecyclerView.setAdapter(adapter);

        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment(String comment){
            TextView comment_text = mView.findViewById(R.id.comments_text);
            comment_text.setText(comment);
        }
        public void setDate(String date){
            TextView comment_date = mView.findViewById(R.id.comments_text_date);
            comment_date.setText(date+": ");
        }
        public void setUsername(String username){
            TextView comment_username = mView.findViewById(R.id.comments_text_username);
            comment_username.setText("@"+username+" ");
        }
    }

    private void validateData(String userName) {
        String comment_text = mEditComment.getText().toString();

        if (TextUtils.isEmpty(comment_text)){
            Toast.makeText(this, "Please write some comment", Toast.LENGTH_SHORT).show();
        }
        else {
            Date date = new Date();
            currentDate = date.toLocaleString();
            randomKey = currentUser + currentDate;
            HashMap map = new HashMap();
            map.put("uid",currentUser);
            map.put("comment",comment_text);
            map.put("date",currentDate);
            map.put("username",userName);

            PostRef.child(randomKey).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                   if (task.isSuccessful()){
                       Toast.makeText(CommentsActivity.this, "Comment updated successfully", Toast.LENGTH_SHORT).show();
                   }
                   else{
                       Toast.makeText(CommentsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                   }
                }
            });

        }
    }
}
