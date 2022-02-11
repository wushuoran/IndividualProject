package com.example.matchandride;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.SavedStateHandle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    /*
    * 还不能重设密码
    * terms and conditions
    * */

    private FirebaseAuth mAuth;
    public static final String TAG = "TAG";
    EditText usernameEditText;
    EditText passwordEditText;
    Button loginButton;
    Button regiButton;
    TextView resetPass;
    TextView readTC;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        usernameEditText = (EditText) findViewById(R.id.username_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        loginButton = (Button) findViewById(R.id.btn_login);
        regiButton = (Button) findViewById(R.id.btn_regi);
        resetPass = (TextView) findViewById(R.id.forget_password);
        readTC = (TextView) findViewById(R.id.terms_and_conditions);

        resetPass.setPaintFlags(resetPass.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        readTC.setPaintFlags(readTC.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        setListeners();

    }

    public void setListeners(){

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userEmail = usernameEditText.getText().toString().trim();
                final String userPass = passwordEditText.getText().toString().trim();
                loginUser(userEmail, userPass);
            }
        });

        regiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userEmail = usernameEditText.getText().toString().trim();
                if (checkInputDetails(userEmail)){
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d(TAG, "sendPasswordResetEmail:success");
                                Toast.makeText(LoginActivity.this, "Password Reset Email Sent.", Toast.LENGTH_LONG).show();
                            }else{
                                Log.d(TAG, "sendPasswordResetEmail:failure"+task.getException());
                                Toast.makeText(LoginActivity.this, "ERROR Sending Email!" + task.getException(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(LoginActivity.this, "Please enter your email in the field first", Toast.LENGTH_LONG).show();
                }
            }
        });

        readTC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, TermsActivity.class);
                startActivity(intent);
            }
        });

    }

    public void loginUser(String userEmail, String userPass){

        if (checkInputDetails(userEmail,userPass)) {
            mAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                    }
                }
            });
        }

    }

    public boolean checkInputDetails(String userEmail, String userPass){

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if ( userEmail.length() == 0 || userPass.length() == 0) {
            Toast.makeText(getApplicationContext(), "Missing Fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!userEmail.matches(emailPattern)) {
            Toast.makeText(getApplicationContext(),"Email Address Invalid",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (userPass.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password should be longer than 6 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

    public boolean checkInputDetails(String userEmail){

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if ( userEmail.length() == 0) {
            Toast.makeText(getApplicationContext(), "Missing Fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!userEmail.matches(emailPattern)) {
            Toast.makeText(getApplicationContext(),"Email Address Invalid",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

}
