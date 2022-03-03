package com.example.matchandride;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class SendInvActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView meetMap;
    private GoogleMap googleMap;
    private LinearLayout invitedUsers;
    private EditText rideNotes;
    private Button sendInv;
    private ArrayList<String> invUids;
    private ViewGroup.MarginLayoutParams params;
    public static FirebaseAuth mAuth;
    public static FirebaseFirestore mStore;
    public static FirebaseStorage mStra;
    public static StorageReference straRef;
    public static DatabaseReference mDbLoc;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_inv);

        invUids = new ArrayList<>();
        getInvData();

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();

        meetMap = (MapView) findViewById(R.id.map_meet_up);
        invitedUsers = (LinearLayout) findViewById(R.id.sv_inv_list_layout);
        rideNotes = (EditText) findViewById(R.id.edit_ride_notes);
        sendInv = (Button) findViewById(R.id.btn_send_inv);

        params = (ViewGroup.MarginLayoutParams) invitedUsers.getLayoutParams();

        meetMap.onCreate(savedInstanceState);
        meetMap.getMapAsync(this);

        setListeners();

    }

    private void setListeners() {

        if (invUids.size()>0){
            for (String uid : invUids){
                TextView tv = new TextView(getApplicationContext());
                tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                tv.setHeight((int) ((int) 45*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f));
                params.topMargin = (int) ((int) 5*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f);
                tv.setLayoutParams(params);
                tv.setTextColor(Color.DKGRAY);
                tv.setTextSize(15);
                tv.setGravity(Gravity.CENTER);
                tv.setBackground(getResources().getDrawable(R.drawable.chart_bound1));
                tv.setText(uid);
                invitedUsers.addView(tv);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SendInvActivity.this);
                        builder.setMessage("Are you sure to remove this user?")
                                .setTitle("Confirm").setCancelable(true);
                        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                invitedUsers.removeView(tv);
                            }
                        });
                        builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.cancel();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }


    @Override
    public void onResume() {
        meetMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        meetMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        meetMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        meetMap.onLowMemory();
    }

    private void getInvData(){
        Bundle extras = this.getIntent().getExtras();
        this.invUids = (ArrayList<String>) extras.get("inviteList");
    }

}
