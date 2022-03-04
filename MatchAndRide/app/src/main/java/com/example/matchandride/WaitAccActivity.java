package com.example.matchandride;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class WaitAccActivity extends AppCompatActivity {

    private LinearLayout joinedUsers;
    private ViewGroup.MarginLayoutParams params;
    private Button startRiding;
    private ArrayList<String> invUids;
    private ArrayList<String> acceptedUser;
    private int userResponseAmt;
    ValueEventListener accValEvnLis;
    public static final String TAG = "TAG";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_users);

        invUids = new ArrayList<String>();
        acceptedUser = new ArrayList<>();
        getInvData();
        userResponseAmt = invUids.size();

        startRiding = (Button) findViewById(R.id.btn_start_ride_inv);
        startRiding.setEnabled(false);
        startRiding.setText("Waiting...");
        joinedUsers = (LinearLayout) findViewById(R.id.sv_users_list_layout);
        params = (ViewGroup.MarginLayoutParams) joinedUsers.getLayoutParams();

        accValEvnLis = new ValueEventListener() {

            ArrayList<String> checkedChild = new ArrayList<>(); // prevent dup checking child

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (userResponseAmt > 0){ // there still some invited user don't send a response
                    for (DataSnapshot sp : snapshot.getChildren()){
                        for (String uid : invUids){
                            String expectChild = uid + ":" + SendInvActivity.mAuth.getCurrentUser().getUid();
                            if (sp.getKey().equals(expectChild) && !checkedChild.contains(sp.getKey())){ // it's a user response
                                userResponseAmt--;
                                boolean isAccepted = Boolean.valueOf(sp.getValue().toString());
                                if (isAccepted){
                                    checkedChild.add(expectChild);
                                    setJoinedUser(uid);
                                }else{
                                    Toast.makeText(WaitAccActivity.this, "One user refused invitation.", Toast.LENGTH_SHORT).show();
                                    if (userResponseAmt == 0) {
                                        SendInvActivity.mDbAcc.removeEventListener(accValEvnLis);      // remove value change listener on rt-acc
                                        for (String id: invUids){                                           // remove invitation response on rt-acc
                                            String childToRemove = id + ":" + SendInvActivity.mAuth.getCurrentUser().getUid();
                                            SendInvActivity.mDbAcc.child(childToRemove).removeValue();
                                        }
                                        finish();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };

        SendInvActivity.mDbAcc.addValueEventListener(accValEvnLis);

    }

    public void setJoinedUser(String uid){
        acceptedUser.add(uid);
        TextView tv = new TextView(getApplicationContext());
        tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        tv.setHeight((int) ((int) 45*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f));
        params.topMargin = (int) ((int) 5*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f);
        tv.setLayoutParams(params);
        tv.setTextColor(Color.DKGRAY);
        tv.setTextSize(15);
        tv.setGravity(Gravity.CENTER);
        tv.setBackground(getResources().getDrawable(R.drawable.chart_bound1));
        setJoinedUserInfo(uid,tv);
        joinedUsers.addView(tv);
        if (userResponseAmt == 0){                                              // all invited users have sent response
            SendInvActivity.mDbAcc.removeEventListener(this.accValEvnLis);      // remove value change listener on rt-acc
            for (String id: invUids){                                           // remove invitation response on rt-acc
                String childToRemove = id + ":" + SendInvActivity.mAuth.getCurrentUser().getUid();
                SendInvActivity.mDbAcc.child(childToRemove).removeValue();
            }
            startRiding.setEnabled(true);
            startRiding.setText("Start!");
            //........record riding
            this.finish();
        }

    }

    public void setJoinedUserInfo(String uid, TextView tv){
        DocumentReference dRef = SendInvActivity.mStore.collection("UserNames").document(uid);
        final String[] nearUserInfo = new String[3];
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        nearUserInfo[0] = document.getString("Username");
                        tv.setText(nearUserInfo[0] + " has joined");
                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void getInvData(){
        Bundle extras = this.getIntent().getExtras();
        this.invUids = (ArrayList<String>) extras.get("invUids");
    }

}
