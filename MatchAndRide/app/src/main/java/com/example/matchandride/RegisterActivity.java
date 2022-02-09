package com.example.matchandride;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText newUserEmail, newUserPass, confirmPass;
    private CheckBox haveReadTC;
    private TextView readTC;
    private Button regiAcc;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);
        mAuth.getInstance();

        newUserEmail = (EditText) findViewById(R.id.new_user_email);
        newUserPass = (EditText) findViewById(R.id.new_user_password);
        confirmPass = (EditText) findViewById(R.id.password_confirm);
        haveReadTC = (CheckBox) findViewById(R.id.have_read_tc);
        readTC = (TextView) findViewById(R.id.terms_and_conditions_reg_page);
        regiAcc = (Button) findViewById(R.id.btn_new_user_reg);

        regiAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }

}
