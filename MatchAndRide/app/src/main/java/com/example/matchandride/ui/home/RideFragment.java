package com.example.matchandride.ui.home;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.matchandride.LoginActivity;
import com.example.matchandride.MainActivity;
import com.example.matchandride.R;
import com.example.matchandride.RecordRideActivity;
import com.example.matchandride.databinding.FragmentRideBinding;

/*
public class RideFragment extends Fragment {


    private RideViewModel rideViewModel;
    private FragmentRideBinding binding;
    private ImageView weeklySum;
    public static Switch onlineSwitch;
    private Spinner bikeSelector;
    private Button startRide, inviteFri, popRoute, roadCon;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rideViewModel =
                new ViewModelProvider(this).get(RideViewModel.class);

        binding = FragmentRideBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        /*
        final TextView textView = binding.textHome;
        rideViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        //weeklySum = (ImageView) root.findViewById(R.id.image_chart);
        onlineSwitch = (Switch) root.findViewById(R.id.switch_online);
        //bikeSelector = (Spinner) root.findViewById(R.id.spin_bikeType);
        startRide = (Button) root.findViewById(R.id.btn_startRiding);
        //inviteFri = (Button) root.findViewById(R.id.btn_invite);
        popRoute = (Button) root.findViewById(R.id.btn_pop_route);
        roadCon = (Button) root.findViewById(R.id.btn_road_con);

        onlineSwitch.setChecked(MainActivity.onlineSwitchStatus);

        String[] bikeTyps = new String[]{"Road", "Mountain", "City", "Other"};
        ArrayAdapter<String> aa = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, bikeTyps);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bikeSelector.setAdapter(aa);

        ((MainActivity)getActivity()).listenToInv();

        setListeners();

        return root;
    }

    public void setListeners(){

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

        startRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // once start riding, tell MainActivity, PLEASE DO NOT SET LISTENER!!!!
                // but need to modify this boolean back if ride finished
                MainActivity.goToOtherAct = true;
                // once started riding, stop receiving invitation (from MainActivity)
                // set a different listener in recording page
                ((MainActivity)getActivity()).removeInvListener();
                Intent intent = new Intent(getActivity(), RecordRideActivity.class);
                intent.putExtra("isOnline", onlineSwitch.isChecked());
                startActivity(intent);
            }
        });

        inviteFri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mAuth.getCurrentUser() == null)
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                else {

                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}*/