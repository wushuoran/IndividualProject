package com.example.matchandride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AccSettingActivity extends AppCompatActivity {

    /*
    * 还不能改头像
    * terms还没写
    * */

    private Button changeUsername, changePic, changePass, changeEmail, readTC;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_setting);

        changeUsername = (Button) findViewById(R.id.btn_change_username_acc);
        changePic = (Button) findViewById(R.id.btn_change_pic_acc);
        changePass = (Button) findViewById(R.id.btn_change_password_acc);
        changeEmail = (Button) findViewById(R.id.btn_change_email_acc);
        readTC = (Button) findViewById(R.id.btn_read_terms_acc);

        setListeners();

    }

    public void setListeners(){

        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccSettingActivity.this, ChangeUsernameActivity.class);
                startActivity(intent);
            }
        });

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccSettingActivity.this, ChangePassActivity.class);
                startActivity(intent);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccSettingActivity.this, ChangeEmailActivity.class);
                startActivity(intent);
            }
        });

        readTC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccSettingActivity.this, TermsActivity.class);
                startActivity(intent);
            }
        });

    }

}
