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


public class RideFragment extends Fragment {


    private RideViewModel rideViewModel;
    private FragmentRideBinding binding;
    private ImageView weeklySum;
    private Switch onlineSwitch;
    private Spinner bikeSelector;
    private Button startRide, inviteFri;

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
        */
        weeklySum = (ImageView) root.findViewById(R.id.image_chart);
        onlineSwitch = (Switch) root.findViewById(R.id.switch_online);
        bikeSelector = (Spinner) root.findViewById(R.id.spin_bikeType);
        startRide = (Button) root.findViewById(R.id.btn_startRiding);
        inviteFri = (Button) root.findViewById(R.id.btn_invite);

        String[] bikeTyps = new String[]{"Road", "Mountain", "City", "Other"};
        ArrayAdapter<String> aa = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, bikeTyps);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bikeSelector.setAdapter(aa);

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

                }
            }
        });

        startRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), RecordRideActivity.class));
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
}