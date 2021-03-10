package com.khan.chitchat;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private TextView mPostDesc;
    private Button mEidtPost,mDeletePost;
    private ImageView mPostImage;
    private String PostKey,description,postImage,currentUserId,databaseUid;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        PostKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        mPostDesc = findViewById(R.id.click_post_edit_desc);
        mPostImage = findViewById(R.id.click_post_image);
        mEidtPost = findViewById(R.id.click_post_edit_button);
        mDeletePost = findViewById(R.id.click_post_delete_button);

        mDeletePost.setVisibility(View.INVISIBLE);
        mEidtPost.setVisibility(View.INVISIBLE);

        mDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPostRef.removeValue();
                goToMainActivity();
                Toast.makeText(ClickPostActivity.this, "Post Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        mEidtPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePost(description);
            }
        });

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    description = dataSnapshot.child("description").getValue().toString();
                    postImage = dataSnapshot.child("postimage").getValue().toString();
                    databaseUid = dataSnapshot.child("uid").getValue().toString();
                    mPostDesc.setText(description);
                    Picasso.with(getApplicationContext()).load(postImage).into(mPostImage);

                    if (currentUserId.equals(databaseUid)){
                        mDeletePost.setVisibility(View.VISIBLE);
                        mEidtPost.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updatePost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");
        final EditText textView = new EditText(ClickPostActivity.this);
        textView.setText(description);
        builder.setView(textView);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickPostRef.child("description").setValue(textView.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_blue_light);
        dialog.show();

    }

    private void goToMainActivity() {
        Intent mainIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
