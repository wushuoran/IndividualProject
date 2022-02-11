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

public class ChangeEmailActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public static final String TAG = "TAG";
    //public static String emailToResetPass;
    private EditText newEmail, confNewEmail;
    private Button resetEmail;
    private TextView currentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_email_page);
        mAuth = FirebaseAuth.getInstance();

        newEmail = (EditText) findViewById(R.id.email_change);
        confNewEmail = (EditText) findViewById(R.id.email_change_confirm);
        resetEmail = (Button) findViewById(R.id.btn_change_email_change_email);
        currentEmail = (TextView) findViewById(R.id.text_current_email_change_email);
        currentEmail.setText(mAuth.getCurrentUser().getEmail());

        setListeners();

    }

    public void setListeners(){

        resetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newemail = newEmail.getText().toString().trim();
                final String confnewemail = confNewEmail.getText().toString().trim();
                if (checkInputDetails(newemail) && checkInputDetails(confnewemail)){
                    if(newemail.equals(confnewemail)){
                        mAuth.getCurrentUser().updateEmail(newemail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, "updateEmail:success");
                                    Intent intent = new Intent(ChangeEmailActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(ChangeEmailActivity.this, "ERROR!" + task.getException(), Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "updateEmail:failure", task.getException());
                                }
                            }
                        });
                    }else Toast.makeText(ChangeEmailActivity.this, "Two emails are different", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean checkInputDetails(String newemail){

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if ( newemail.length() == 0) {
            Toast.makeText(getApplicationContext(), "Missing Fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!newemail.matches(emailPattern)) {
            Toast.makeText(getApplicationContext(),"Email Address Invalid",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

}
