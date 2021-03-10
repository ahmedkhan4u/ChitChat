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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {
    private EditText mLoginEmail, mLoginPassword;
    private Button mLoginButton;
    private FirebaseAuth mAuth;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //...........................................
        mAuth = FirebaseAuth.getInstance();
        //...........................................

        mLoginEmail = findViewById(R.id.edt_login_email);
        mLoginPassword = findViewById(R.id.edt_login_password);
        mLoginButton = findViewById(R.id.btn_login);
        dialog = new ProgressDialog(this);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
            }
        });

    }

    private void LoginUser() {
        String email = mLoginEmail.getText().toString();
        String password = mLoginPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
        }
        else {
            dialog.setMessage("Checking User Account");
            dialog.setTitle("Please Wait");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        goToMainActivity();
                        dialog.dismiss();
                        Toast.makeText(Login.this, "User Loged in Successful", Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException().getMessage();
                        dialog.dismiss();
                        Toast.makeText(Login.this, error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void goToMainActivity() {
        Intent loginIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(loginIntent);
        finish();
    }

    public void TextClick(View view) {
        goToRegiterActivity();
    }

    private void goToRegiterActivity() {
        Intent registerIntent = new Intent(getApplicationContext(),Register.class);
        startActivity(registerIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current_user = mAuth.getCurrentUser();
        if (current_user != null){
            goToMainActivity();
        }
    }
}
