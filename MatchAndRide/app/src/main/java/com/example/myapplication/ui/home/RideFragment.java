package com.example.myapplication.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentRideBinding;

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
        onlineSwitch.setEnabled(MainActivity.loginStatus);
        bikeSelector = (Spinner) root.findViewById(R.id.spin_bikeType);
        startRide = (Button) root.findViewById(R.id.btn_startRiding);
        inviteFri = (Button) root.findViewById(R.id.btn_invite);
        inviteFri.setEnabled(MainActivity.loginStatus);


        String[] bikeTyps = new String[]{"Road", "Mountain", "City", "Other"};
        ArrayAdapter<String> aa = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, bikeTyps);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bikeSelector.setAdapter(aa);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}