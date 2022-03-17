package com.example.matchandride;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.matchandride.tools.AccFriendsAdapter;
import com.example.matchandride.tools.AddFriendsAdapter;
import com.example.matchandride.tools.RateUsersAdapter;
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

public class FindFriendsActivity  extends AppCompatActivity {

    private Button backBtn;
    private ListView listViewIncome, listViewReco;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    public static Activity findFri;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        findFri = this;

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();

        backBtn = (Button) findViewById(R.id.btn_back_to_me);
        listViewIncome = (ListView) findViewById(R.id.lv_income_fri);
        listViewReco = (ListView) findViewById(R.id.lv_add_fri);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mStore.collection("UserRatings").document(mAuth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            Map<String,Object> info = task.getResult().getData();
                            HashMap<Integer, String> historyUsers = new HashMap<>();
                            if (info != null){
                                System.out.println("found " + info.size() + " history users");
                                int count = 0;
                                for (String uid : info.keySet()) {
                                    historyUsers.put(count,uid);
                                    count++;
                                }
                                mStore.collection("UserFriends").document(mAuth.getCurrentUser().getUid()).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()){
                                                    Map<String, Object> friends = task.getResult().getData();
                                                    if (friends != null){
                                                        System.out.println("User has friend" + friends);
                                                        int reqCount = 0;
                                                        HashMap<Integer,String> incomeReqs = new HashMap<>();
                                                        for (String friUid: friends.keySet()){ // check if current user has this history user as a friend
                                                            int index = -1;
                                                            for (int i : historyUsers.keySet()){
                                                                if (historyUsers.get(i).equals(friUid)) index = i;
                                                            }
                                                            System.out.println("index will be removed " + index);
                                                            if (index != -1) historyUsers.remove(index);
                                                            if (!(boolean)friends.get(friUid)){ // if there's an incoming request (field value false)
                                                                incomeReqs.put(reqCount, friUid);
                                                                reqCount++;
                                                            }
                                                        }
                                                        if (incomeReqs.size()>0) listViewIncome.setAdapter(
                                                                new AccFriendsAdapter(FindFriendsActivity.this, incomeReqs));
                                                    }
                                                    HashMap<Integer,String> historyUsers2 = new HashMap<>();
                                                    int index2 = 0;
                                                    for (int i : historyUsers.keySet()){ // re-construct a hashmap, with index start from 0
                                                        historyUsers2.put(index2, historyUsers.get(i));
                                                        index2++;
                                                    }
                                                    listViewReco.setAdapter(new AddFriendsAdapter(FindFriendsActivity.this, historyUsers2));
                                                }
                                            }
                                        });
                            }

                        }
                    }
                });



    }


}
