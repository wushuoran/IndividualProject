package com.example.matchandride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.SavedStateHandle;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private SavedStateHandle savedStateHandle;
    HashMap <String,String> sampleUsers = new HashMap<>();
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        sampleUsers.put("wsr","12345");
        sampleUsers.put("kyq","12345");
        EditText usernameEditText = (EditText) findViewById(R.id.username_edit_text);
        EditText passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        Button loginButton = (Button) findViewById(R.id.btn_login);
        Button regiButton = (Button) findViewById(R.id.btn_regi);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean loggedIn = login(usernameEditText.getText().toString(),passwordEditText.getText().toString());
                if(loggedIn){
                    System.out.println("Logged In");
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
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

    public boolean login (String username, String password){
        if (sampleUsers.containsKey(username)){
            if (sampleUsers.get(username).equals(password)){
                MainActivity.loginStatus = true;
                return true;
            }else return false;
        }else{
            return false;
        }
    }

}
