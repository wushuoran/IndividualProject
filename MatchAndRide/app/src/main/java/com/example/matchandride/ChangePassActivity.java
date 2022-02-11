package com.example.matchandride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePassActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public static final String TAG = "TAG";
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
        currentEmail.setText(mAuth.getCurrentUser().getEmail());

        setListeners();

    }

    public void setListeners(){

        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newpass = newPass.getText().toString().trim();
                final String confnewpass = confNewPass.getText().toString().trim();
                if (checkInputDetails(newpass) && checkInputDetails(confnewpass)){
                    if(newpass.equals(confnewpass)){
                        mAuth.getCurrentUser().updatePassword(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, "updatePassword:success");
                                    Intent intent = new Intent(ChangePassActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(ChangePassActivity.this, "ERROR!" + task.getException(), Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "updatePassword:failure", task.getException());
                                }
                            }
                        });
                    }else Toast.makeText(ChangePassActivity.this, "Two passwords are different", Toast.LENGTH_SHORT).show();
                }
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
