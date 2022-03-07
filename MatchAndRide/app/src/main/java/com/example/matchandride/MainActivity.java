package com.example.matchandride;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.matchandride.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    public static boolean loginStatus = false;
    private ActivityMainBinding binding;
    public static FirebaseAuth mAuth;
    public static FirebaseFirestore mStore;
    public static FirebaseStorage mStra;
    public static StorageReference straRef;
    public static DatabaseReference mDbLoc;
    public static DatabaseReference mDbInv;
    public static DatabaseReference mDbAcc;
    public static DatabaseReference mDbInvited;
    public static boolean onlineSwitchStatus;
    public static boolean isBackground;
    private ValueEventListener invEvnLis;
    public static boolean isInvited;
    public static boolean goToOtherAct = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        registerComponentCallbacks(this);
        getApplication().registerActivityLifecycleCallbacks(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();
        mDbLoc = FirebaseDatabase.getInstance().getReference("rt-location");
        mDbInv = FirebaseDatabase.getInstance().getReference("rt-invitation");
        mDbAcc = FirebaseDatabase.getInstance().getReference("rt-accept");
        mDbInvited = FirebaseDatabase.getInstance().getReference("rt-invited");

        if (mAuth.getCurrentUser() != null){
            loginStatus = true;
        }



        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        //AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
        //        R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
        //        .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    @Override
    public void onTrimMemory(final int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            isBackground = true;
            mDbLoc.child(mAuth.getUid()).removeValue();
            System.out.println("background");
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if(isBackground){
            isBackground = false;
            System.out.println("foreground");
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public void listenToInv() {
        if (MainActivity.onlineSwitchStatus && !goToOtherAct && !(invEvnLis!=null)) {
            invEvnLis = new ValueEventListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        String childname = sp.getKey();
                        String[] splitChild = childname.split(":");
                        String invSender = splitChild[0];
                        String invReci = splitChild[1];
                        if (invReci.equals(MainActivity.mAuth.getCurrentUser().getUid())) {
                            System.out.println("Invitation Detected!");

                            String invMsg = sp.getValue().toString();
                            String[] splitMsg = invMsg.split(",");
                            LatLng meetPlace = new LatLng(Double.valueOf(splitMsg[0]), Double.valueOf(splitMsg[1]));
                            int estParti = Integer.valueOf(splitMsg[2]);
                            try{
                                //Toast.makeText(MainActivity.this, "One Invitation Received !\nview details in Discover page.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, AcceptInvActivity.class);
                                intent.putExtra("meetPlace", meetPlace);
                                intent.putExtra("senderUid", invSender);
                                intent.putExtra("estParti", estParti);
                                intent.putExtra("curAct", "main");
                                if (splitMsg.length == 4)intent.putExtra("addNotes", splitMsg[3]);
                                MainActivity.mDbInv.removeEventListener(invEvnLis);
                                startActivity(intent);
                            }catch(Exception e){e.printStackTrace();}
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            MainActivity.mDbInv.addValueEventListener(invEvnLis);
            System.out.println("Accept RTDB Listener Set");
        }
    }

    public void removeInvListener(){
        try{mDbInv.removeEventListener(invEvnLis);
        }catch(Exception e){e.printStackTrace();}
    }


/*
    @Override
    protected void onStop() {
        System.out.println("app quits");
        mDbLoc.child(mAuth.getUid()).removeValue();
        super.onStop();
    }

 */

}