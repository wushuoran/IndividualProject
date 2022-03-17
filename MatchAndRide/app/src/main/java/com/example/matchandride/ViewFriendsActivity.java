package com.example.matchandride;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.matchandride.tools.FriendListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewFriendsActivity extends AppCompatActivity {

    private Button back;
    private ListView listView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    public static Activity viewFri;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        viewFri = this;

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();

        listView = (ListView) findViewById(R.id.lv_fri_list);
        back = (Button) findViewById(R.id.btn_list_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mStore.collection("UserFriends").document(mAuth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            Map<String, Object> friends = task.getResult().getData();
                            if (friends!=null){
                                int friCount = 0;
                                HashMap<Integer, String> friendAda = new HashMap<>();
                                for (String fri : friends.keySet()){
                                    if ((boolean) friends.get(fri)){
                                        friendAda.put(friCount,fri);
                                        friCount++;
                                    }
                                }
                                System.out.println(friendAda);
                                if (friendAda.size()>0)
                                    listView.setAdapter(new FriendListAdapter(ViewFriendsActivity.this, friendAda));
                            }
                        }
                    }
                });


    }

}
