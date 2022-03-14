package com.example.matchandride.ui.notifications;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.matchandride.AccSettingActivity;
import com.example.matchandride.LoginActivity;
import com.example.matchandride.MainActivity;
import com.example.matchandride.ManageRideActivity;
import com.example.matchandride.R;
import com.example.matchandride.databinding.FragmentMeBinding;
import com.example.matchandride.objects.RideObject;
//import com.example.matchandride.ui.home.RideFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MeFragment extends Fragment {

    private MeViewModel meViewModel;
    private FragmentMeBinding binding;
    public static final String TAG = "TAG";
    Button editPro, manageRide, friReq, friList, accSetting, loginBtn;
    ImageView portrait;
    public static Switch onlineSwitch;
    TextView userName, avgSpeed, rating;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        meViewModel = new ViewModelProvider(this).get(MeViewModel.class);

        binding = FragmentMeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //editPro = (Button) root.findViewById(R.id.btn_edit_profile);
        manageRide = (Button) root.findViewById(R.id.btn_manage_ride);
        friReq = (Button) root.findViewById(R.id.btn_fri_req);
        friList = (Button) root.findViewById(R.id.btn_fri_list);
        accSetting = (Button) root.findViewById(R.id.btn_account_set);
        loginBtn = (Button) root.findViewById(R.id.btn_login);
        portrait = (ImageView) root.findViewById(R.id.img_port);
        userName = (TextView) root.findViewById(R.id.text_username);
        avgSpeed = (TextView) root.findViewById(R.id.text_avg_speed);
        rating = (TextView) root.findViewById(R.id.text_user_rating);
        onlineSwitch = (Switch) root.findViewById(R.id.switch_online);
        onlineSwitch.setChecked(MainActivity.onlineSwitchStatus);

        if(MainActivity.mAuth.getCurrentUser() != null) {
            try {
                updateUserInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setListeners();

        ((MainActivity)getActivity()).listenToInv();

        return root;

    }

    public void updateUserInfo() throws IOException {

        FirebaseUser currentUser = MainActivity.mAuth.getCurrentUser();
        DocumentReference dRef = MainActivity.mStore.collection("UserNames").document(currentUser.getUid());
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        /* GET&SET MORE INFO HERE, AVG SPEED, RATING, .... */
                        userName.setText(document.getString("Username"));
                        avgSpeed.setText("History AVG Speed: " + document.get("AVGspd").toString() + "kph");
                        rating.setText("Rating: " + document.get("Rating").toString() + "/5");
                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        String cloudStoragePath = "UserProfilePics/" + MainActivity.mAuth.getCurrentUser().getUid();
        System.out.println(cloudStoragePath);
        File localFile = File.createTempFile("images","jpg");
        MainActivity.straRef.child(cloudStoragePath).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                try {
                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.straRef..getContentResolver(), uri);
                    portrait.setImageURI(Uri.parse(localFile.toString()));
                    //System.out.println("Profile Picture Updated!!!!!!!!!!!!");
                    //Toast.makeText(getActivity(), "Profile Picture Updated!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    public void setListeners(){

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }else{
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    try{MainActivity.mDbLoc.child(MainActivity.mAuth.getUid()).removeValue();}catch(Exception e){e.printStackTrace();}
                    MainActivity.mAuth.signOut();
                    MainActivity.onlineSwitchStatus = false;
                    Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();

                }
            }
        });

        friReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mAuth.getCurrentUser() == null)
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                else {

                }
            }
        });

        friList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mAuth.getCurrentUser() == null)
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                else {

                }
            }
        });

        accSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mAuth.getCurrentUser() == null)
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                else {
                    startActivity(new Intent(getActivity(), AccSettingActivity.class));
                }
            }
        });

        manageRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ManageRideActivity.class));
            }
        });

        onlineSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mAuth.getCurrentUser() == null) {
                    onlineSwitch.setChecked(false);
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }else {
                    if (!onlineSwitch.isChecked()){
                        try{MainActivity.mDbLoc.child(MainActivity.mAuth.getUid()).removeValue();}catch(Exception e){e.printStackTrace();}
                        MainActivity.onlineSwitchStatus = false;
                    }else{
                        MainActivity.onlineSwitchStatus = true;
                    }
                }
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}