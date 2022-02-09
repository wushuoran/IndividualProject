package com.example.matchandride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        EditText usernameEditText = (EditText) findViewById(R.id.username_edit_text);
        EditText passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        Button loginButton = (Button) findViewById(R.id.btn_login);
        Button regiButton = (Button) findViewById(R.id.btn_regi);

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

}
