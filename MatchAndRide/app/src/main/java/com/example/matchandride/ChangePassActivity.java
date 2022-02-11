package com.example.matchandride;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePassActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public static String emailToResetPass;
    private EditText newPass, confNewPass;
    private Button resetPass;
    private TextView currentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_page);
        mAuth = FirebaseAuth.getInstance();

        newPass = (EditText) findViewById(R.id.password_change);
        confNewPass = (EditText) findViewById(R.id.password_change_confirm);
        resetPass = (Button) findViewById(R.id.btn_change_password);
        currentEmail = (TextView) findViewById(R.id.text_current_email);
        currentEmail.setText(emailToResetPass);

        setListeners();

    }

    public void setListeners(){

        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });

    }

    public boolean checkInputDetails(String userPass){

        if ( userPass.length() == 0) {
            Toast.makeText(getApplicationContext(), "Missing Fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (userPass.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password should be longer than 6 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

}
