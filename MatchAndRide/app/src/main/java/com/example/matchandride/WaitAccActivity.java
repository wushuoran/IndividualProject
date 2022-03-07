package com.example.matchandride;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import java.util.HashMap;

public class WaitAccActivity extends AppCompatActivity {

    private LinearLayout joinedUsers;
    private ViewGroup.MarginLayoutParams params;
    private Button startRiding;
    private ArrayList<String> invUids;
    private ArrayList<String> acceptedUser,refusedUser;
    private int userResponseAmt;
    ValueEventListener accValEvnLis;
    public static final String TAG = "TAG";
    private HashMap<String, TextView> userTV;
    private ArrayList<String> dontWait;
    private MyCountDown timer;
    private ArrayList<String> completeInvUid; // used for clearing data in server

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_users);

        invUids = new ArrayList<String>();
        acceptedUser = new ArrayList<>();
        refusedUser = new ArrayList<>();
        dontWait = new ArrayList<>();
        completeInvUid = new ArrayList<>();
        userTV = new HashMap<>();
        getInvData();
        userResponseAmt = invUids.size();

        timer = new MyCountDown(91000, 1000);

        startRiding = (Button) findViewById(R.id.btn_start_ride_inv);
        startRiding.setEnabled(false);
        startRiding.setText("Waiting...");
        joinedUsers = (LinearLayout) findViewById(R.id.sv_users_list_layout);
        params = (ViewGroup.MarginLayoutParams) joinedUsers.getLayoutParams();

        accValEvnLis = new ValueEventListener() {

            ArrayList<String> checkedChild = new ArrayList<>(); // prevent dup checking child

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkLostUser();
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
                                }else {
                                    Toast.makeText(WaitAccActivity.this, "One user refused invitation.", Toast.LENGTH_SHORT).show();
                                    refusedUser.add(uid);
                                    if (userResponseAmt == 0 && acceptedUser.size()==0) {
                                        SendInvActivity.mDbAcc.removeEventListener(accValEvnLis);      // remove value change listener on rt-acc
                                        for (String id : invUids) {                                           // remove invitation response on rt-acc
                                            String childToRemove = id + ":" + SendInvActivity.mAuth.getCurrentUser().getUid();
                                            SendInvActivity.mDbAcc.child(childToRemove).removeValue();
                                        }
                                        timer.allHaveResult = true;
                                        invitationFailed();
                                    }else if(userResponseAmt == 0 && acceptedUser.size()!=0){
                                        isTimeToStart();
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
        userTV.put(uid,tv);

        isTimeToStart();
    }

    public void isTimeToStart(){
        if (userResponseAmt == 0){                                              // all invited users have sent response
            SendInvActivity.mDbAcc.removeEventListener(this.accValEvnLis);      // remove value change listener on rt-acc
            String currentUserId = SendInvActivity.mAuth.getCurrentUser().getUid();
            String groupMembers = currentUserId;
            for (String id: invUids){                                           // remove invitation response on rt-acc
                String childToRemove = id + ":" + SendInvActivity.mAuth.getCurrentUser().getUid();
                SendInvActivity.mDbAcc.child(childToRemove).removeValue();
                SendInvActivity.mDbInvited.child(id).setValue(true);
                groupMembers = groupMembers + "," + id;
            }
            SendInvActivity.mDbGrp.child(currentUserId).setValue(groupMembers);
            SendInvActivity.mDbInvited.child(currentUserId).setValue(true);
            startRiding.setEnabled(true);
            startRiding.setText("Start!");
            timer.allHaveResult = true;
            //........record riding
            Intent intent = new Intent(WaitAccActivity.this, RecordRideInvActivity.class);
            intent.putExtra("sender", currentUserId);
            intent.putExtra("organizer", true);
            startActivity(intent);
            SendInvActivity.actRef.finish();
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

    public void checkLostUser(){
        if (SendInvActivity.lostUsers.size()!=0){ // if invited user is disconnected
            for (String lostUser: SendInvActivity.lostUsers){ // for each lost user
                if (invUids.contains(lostUser)){ // double check he/she is in invite list
                    //invUids.remove(lostUser);   //remove from inv list
                    if (acceptedUser.contains(lostUser)){
                        this.joinedUsers.removeView(this.userTV.get(lostUser)); // remove view
                        acceptedUser.remove(lostUser);  //remove from accepted user
                        if (acceptedUser.size()==0){
                            timer.allHaveResult = true;
                            invitationFailed();
                        }
                    } else if(refusedUser.contains(lostUser)){

                    } else if(!dontWait.contains(lostUser)){ //this user hasn't sent response
                        userResponseAmt--; //do not wait
                        dontWait.add(lostUser);
                        String invToDelete = SendInvActivity.mAuth.getCurrentUser().getUid() + ":" + lostUser;
                        SendInvActivity.mDbInv.child(invToDelete).removeValue();
                    }
                }
            }
        }
    }

    public void invitationFailed(){
        for (String uid : completeInvUid){
            String expInvName = SendInvActivity.mAuth.getCurrentUser().getUid() + ":" + uid;
            String expAccName = uid + ":" + SendInvActivity.mAuth.getCurrentUser().getUid();
            System.out.println("inv to delete: " + expInvName);
            System.out.println("acc to delete: " + expAccName);
            try{
                SendInvActivity.mDbAcc.child(expAccName).removeValue();
                SendInvActivity.mDbInv.child(expInvName).removeValue();
            }catch(Exception e){e.printStackTrace();}
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(WaitAccActivity.this);
        builder.setMessage("Click OK to go back.")
                .setTitle("Invitation Failed").setCancelable(true);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(WaitAccActivity.this, MainActivity.class));
                SendInvActivity.actRef.finish(); finish();
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void getInvData(){
        Bundle extras = this.getIntent().getExtras();
        this.invUids = (ArrayList<String>) extras.get("invUids");
        this.completeInvUid = (ArrayList<String>) extras.get("invUids");
    }

    private class MyCountDown extends CountDownTimer {

        int secs; boolean allHaveResult = false;

        public MyCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            start();
        }

        @Override
        public void onFinish() {
            secs = 10;
            if (!allHaveResult)
                invitationFailed();
        }

        @Override
        public void onTick(long duration) {
            secs = secs - 1;
        }
    }

}
