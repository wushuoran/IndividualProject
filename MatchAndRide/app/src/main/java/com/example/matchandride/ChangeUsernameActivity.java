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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChangeUsernameActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public FirebaseFirestore mStore;
    public static final String TAG = "TAG";
    private EditText newUsername;
    private Button changeUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_username_page);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        newUsername = (EditText) findViewById(R.id.username_change);
        changeUsername = (Button) findViewById(R.id.btn_change_username_change_username);
        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newusername = newUsername.getText().toString().trim();
                if ( newusername.length() > 0 && newusername.length()<21){
                    Map<String,Object> userInfo = new HashMap<>();
                    userInfo.put("Username", newusername);
                    userInfo.put("Email", mAuth.getCurrentUser().getEmail());
                    mStore.collection("UserNames").document(mAuth.getCurrentUser().getUid()).set(userInfo)
                            .addOnCompleteListener((OnCompleteListener<Void>) (aVoid) -> {
                                Log.d(TAG, "DocumentSnapshot added");
                                Toast.makeText(ChangeUsernameActivity.this, "Username Changed.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(ChangeUsernameActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            Toast.makeText(ChangeUsernameActivity.this, "Failed."+e, Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    Toast.makeText(ChangeUsernameActivity.this, "Username length should between 1~20!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
