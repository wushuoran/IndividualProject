package com.example.matchandride;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText newUSerName, newUserEmail, newUserPass, confirmPass;
    private CheckBox haveReadTC;
    private TextView readTC;
    private Button regiAcc;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);
        mAuth = FirebaseAuth.getInstance();

        newUSerName = (EditText) findViewById(R.id.new_user_name);
        newUserEmail = (EditText) findViewById(R.id.new_user_email);
        newUserPass = (EditText) findViewById(R.id.new_user_password);
        confirmPass = (EditText) findViewById(R.id.password_confirm);
        haveReadTC = (CheckBox) findViewById(R.id.have_read_tc);
        readTC = (TextView) findViewById(R.id.terms_and_conditions_reg_page);
        regiAcc = (Button) findViewById(R.id.btn_new_user_reg);

        setRegiAccListener();

    }

    public void setRegiAccListener(){

        regiAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String userName = newUSerName.getText().toString();
                final String userEmail = newUserEmail.getText().toString().trim();
                final String userPass = newUserPass.getText().toString().trim();
                final String confPass = confirmPass.getText().toString().trim();

                if (checkInputDetails(userName, userEmail, userPass, confPass)) {
                    if (!haveReadTC.isChecked()) {
                        System.out.println(userEmail + " " + userPass);
                        mAuth.createUserWithEmailAndPassword(userEmail, userPass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            MainActivity.loginStatus = true;
                                            MainActivity.userName = userName;
                                            Toast.makeText(RegisterActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "ERROR!" + task.getException(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else{
                        Toast.makeText(RegisterActivity.this, "Please agree with the terms & conditions", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

    }

    public boolean checkInputDetails(String userName, String userEmail, String userPass, String confPass){

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (userName.length() > 25) {
            Toast.makeText(getApplicationContext(), "Username should be less than 25 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (userName.length() == 0 || userEmail.length() == 0 || userPass.length() == 0 || confPass.length()==0) {
            Toast.makeText(getApplicationContext(), "Missing Fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!userEmail.matches(emailPattern)) {
            Toast.makeText(getApplicationContext(),"Email Address Invalid",Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!userPass.equals(confPass)){
            Toast.makeText(getApplicationContext(), "Password not matching", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (userPass.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password should be longer than 6 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }


}
