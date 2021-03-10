package com.khan.chitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.renderscript.Script;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private CircleImageView mProfileImage;
    private EditText mUserName,mFullName,mCountry;
    private Button mSaveButton;
    private Uri imageUri;
    private String currentUser;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        //.......................................................
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorage = FirebaseStorage.getInstance().getReference().child("profile images");
        mProfileImage = findViewById(R.id.user_profile);
        currentUser = mAuth.getCurrentUser().getUid();
        mUserName = findViewById(R.id.edt_profile_username);
        mFullName = findViewById(R.id.edt_profile_full_name);
        mCountry = findViewById(R.id.edt_profile_country);
        mSaveButton = findViewById(R.id.btn_save_profile_data);
        dialog = new ProgressDialog(this);
        //.......................................................

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProfileImage();
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUserName.getText().toString();
                String fullname = mFullName.getText().toString();
                String country = mCountry.getText().toString();
                if (TextUtils.isEmpty(username)){
                    Toast.makeText(SetupActivity.this, "Please Enter Username", Toast.LENGTH_SHORT).show();
                }
                else if (username.length()<4 || username.length()>36){
                    Toast.makeText(SetupActivity.this, "Username Length too Long or too Short", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(fullname)){
                    Toast.makeText(SetupActivity.this, "Please Enter Full User Name", Toast.LENGTH_SHORT).show();
                }
                else if (fullname.length()<4 || fullname.length()>36){
                    Toast.makeText(SetupActivity.this, "Full Name Length too Long or too Short", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(country)){
                    Toast.makeText(SetupActivity.this, "Please Enter Your Country Name", Toast.LENGTH_SHORT).show();
                }
                else if (country.length()<4 || country.length()>26){
                    Toast.makeText(SetupActivity.this, "Country Name Length too Long or too Short", Toast.LENGTH_SHORT).show();
                }
                else if (imageUri == null){
                    Toast.makeText(SetupActivity.this, "Please Choose Profile Image", Toast.LENGTH_SHORT).show();
                }
                else {
                    dialog.setTitle("Please Wait");
                    dialog.setMessage("Updatind Data In Progress");
                    dialog.setCanceledOnTouchOutside(false);
                    HashMap map = new HashMap();
                    map.put("username",username);
                    map.put("fullname",fullname);
                    map.put("country",country);

                    mDatabase.child(currentUser).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SetupActivity.this, "Profile data updated successfully", Toast.LENGTH_SHORT).show();
                                goToMainActivity();
                                dialog.dismiss();
                            }
                            else {
                                Toast.makeText(SetupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
        });

    }

    private void goToMainActivity() {
        Intent mainActivityIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    private void uploadProfileImage() {
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
                mProfileImage.setImageURI(imageUri);
                dialog.setTitle("Please Wait");
                dialog.setMessage("Uploading Profile Image In Progress");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                final StorageReference reference = mStorage.child(currentUser+".jpg");
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
                            String downloadUri = task.getResult().toString();
                            mDatabase.child(currentUser).child("profile image").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(SetupActivity.this, "Image Save Successfully", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        } else {
                            dialog.dismiss();
                            Toast.makeText(SetupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                dialog.dismiss();
                Exception error = result.getError();
            }
        }
    }
}
