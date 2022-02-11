package com.example.matchandride.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.matchandride.AccSettingActivity;
import com.example.matchandride.LoginActivity;
import com.example.matchandride.MainActivity;
import com.example.matchandride.R;
import com.example.matchandride.databinding.FragmentMeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;

public class MeFragment extends Fragment {

    private MeViewModel meViewModel;
    private FragmentMeBinding binding;
    public static final String TAG = "TAG";
    Button editPro, manageRide, friReq, friList, accSetting, loginBtn;
    ImageView portrait;
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

        if(MainActivity.mAuth.getCurrentUser() != null) updateUserInfo();
        setListeners();

        return root;

    }

    public void updateUserInfo(){

        FirebaseUser currentUser = MainActivity.mAuth.getCurrentUser();
        DocumentReference dRef = MainActivity.mStore.collection("UserNames").document(currentUser.getUid());
        String[] userInfo = new String[2];
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userInfo[0] = document.getString("Username");
                        /* GET&SET MORE INFO HERE, AVG SPEED, RATING, .... */
                        userName.setText(userInfo[0]);
                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                        System.out.println( "Current username: " + userInfo[0]);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
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
                    MainActivity.mAuth.signOut();
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

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}