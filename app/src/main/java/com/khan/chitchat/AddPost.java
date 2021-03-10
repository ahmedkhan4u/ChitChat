package com.khan.chitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Date;
import java.util.HashMap;

public class AddPost extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,postRef;
    private StorageReference mStorage;
    private EditText mPostDescription;
    private ImageButton mPostImage;
    private String current_user;
    private Uri imageUri;
    private String downloadUrl;
    private Button mUpdatePost;
    private Date date;
    private String randomNumber, currentDate;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //Fire Base Inflation........................................................................
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mStorage = FirebaseStorage.getInstance().getReference();
        current_user = mAuth.getCurrentUser().getUid();
        //...........................................................................................
        //View Inflation.............................................................................
        mPostImage = findViewById(R.id.update_post_image);
        mPostDescription = findViewById(R.id.update_post_edt_description);
        mUpdatePost = findViewById(R.id.btn_update_post);
        dialog = new ProgressDialog(this);
        //........................................................................

    }

    public void ButtonClick(View view) {
        switch (view.getId()){
            case R.id.update_post_back_btn:
                goToMainActivity();
                break;

            case R.id.update_post_image:
                saveImagetToFirebaseStorage();
                break;

            case R.id.btn_update_post:
                String description = mPostDescription.getText().toString();
                dialog.setMessage("Udating Data in Progress");
                dialog.setTitle("Udating Data");
                dialog.setCanceledOnTouchOutside(false);
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(this, "Please Write Some Description", Toast.LENGTH_SHORT).show();
                }
                else if (imageUri == null){
                    Toast.makeText(this, "Choose Image", Toast.LENGTH_SHORT).show();
                }
                else {
                    dialog.show();
                    saveDataToFirebaseStorage();
                }
                break;
        }
    }

    private void saveDataToFirebaseStorage() {
        mDatabase.child(current_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String description = mPostDescription.getText().toString();
                    String fullName = dataSnapshot.child("fullname").getValue().toString();
                    String profileImage = dataSnapshot.child("profile image").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid",current_user);
                    postMap.put("date",currentDate);
                    postMap.put("description",description);
                    postMap.put("postimage",downloadUrl);
                    postMap.put("profileimage",profileImage);
                    postMap.put("fullname",fullName);

                    postRef.child(current_user+randomNumber).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                Toast.makeText(AddPost.this, "Data Updated Successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                finish();
                                goToMainActivity();
                            }
                            else {
                                Toast.makeText(AddPost.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });

                }else {
                    Toast.makeText(AddPost.this, "No Data Exists", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveImagetToFirebaseStorage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                mPostImage.setImageURI(imageUri);
                date = new Date();
                randomNumber = date.toLocaleString();
                currentDate = date.toLocaleString();
                dialogBox();
                final StorageReference reference = mStorage.child("Post Images").child(imageUri.getLastPathSegment()+randomNumber+".jpg");
                Task uploadTask = reference.putFile(imageUri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            dialog.dismiss();
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUrl = task.getResult().toString();
                            //Toast.makeText(AddPost.this, downloadUrl, Toast.LENGTH_LONG).show();
                            Toast.makeText(AddPost.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                dialog.dismiss();
                Exception error = result.getError();
            }
        }
    }

    private void dialogBox() {
        dialog.setMessage("Image Uploading In Progress Please Wait");
        dialog.setTitle("Uploading Image");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void goToMainActivity() {
        Intent mainIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
