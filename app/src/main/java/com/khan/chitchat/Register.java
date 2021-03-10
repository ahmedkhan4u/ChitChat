package com.khan.chitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Register extends AppCompatActivity {
    private EditText mEmail,mPassword,mConfirmPassword;
    private TextView mSignUp;
    private Button mButtonSignup;
    private FirebaseAuth mAuth;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //..................................................
        mAuth = FirebaseAuth.getInstance();
        //..................................................
        mEmail = findViewById(R.id.edt_register_email);
        mPassword = findViewById(R.id.edt_register_password);
        mConfirmPassword = findViewById(R.id.edt_register_confirm_password);
        mSignUp = findViewById(R.id.txt_register_login);
        mButtonSignup = findViewById(R.id.btn_signup);
        dialog = new ProgressDialog(this);
        mButtonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUserToFirebase();
            }
        });
    }

    private void RegisterUserToFirebase() {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String confirmPassword = mConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Email Address", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Please Enter Confirm Password", Toast.LENGTH_LONG).show();
            }
        else if (email.length()>36){
            Toast.makeText(this, "Email too long", Toast.LENGTH_SHORT).show();
        }
        else if (password.length() < 6){
            Toast.makeText(this, "Password length must be equal to or greater than six", Toast.LENGTH_LONG).show();
        }
        else if (!password.equals(confirmPassword)){
            Toast.makeText(this, "Your password does't match with confirm password", Toast.LENGTH_LONG).show();
        }
        else {
            dialog.setMessage("Authentication in progress");
            dialog.setTitle("Please Wait");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Intent registerIntent = new Intent(getApplicationContext(),SetupActivity.class);
                        startActivity(registerIntent);
                        finish();
                        dialog.dismiss();
                        Toast.makeText(Register.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                    }else{
                        String error = task.getException().getMessage();
                        Toast.makeText(Register.this, error, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    public void TextClick(View view) {
        Intent loginIntent = new Intent(getApplicationContext(),Login.class);
        startActivity(loginIntent);
    }
}
