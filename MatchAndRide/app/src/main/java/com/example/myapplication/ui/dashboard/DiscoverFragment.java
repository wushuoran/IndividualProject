package com.example.myapplication.ui.dashboard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentDiscoverBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

public class DiscoverFragment extends Fragment implements OnMapReadyCallback{

    private DiscoverViewModel discoverViewModel;
    private FragmentDiscoverBinding binding;
    private Switch bikeFilter;
    private Button addList, sendInv, popRoute, roadCon;
    private MapView nearbyMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        discoverViewModel =
                new ViewModelProvider(this).get(DiscoverViewModel.class);

        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        /*        final TextView textView = binding.textDashboard;
        discoverViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        bikeFilter = (Switch) root.findViewById(R.id.switch_filter);
        nearbyMap = (MapView) root.findViewById(R.id.map_nearby);
        addList = (Button) root.findViewById(R.id.btn_add_to_list);
        sendInv = (Button) root.findViewById(R.id.btn_send_inv);
        popRoute = (Button) root.findViewById(R.id.btn_pop_route);
        roadCon = (Button) root.findViewById(R.id.btn_road_con);

        nearbyMap.onCreate(savedInstanceState);
        nearbyMap.getMapAsync(this);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);
        //googleMap.setMyLocationEnabled(true);
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
               .title("Marker"));
    }

    @Override
    public void onResume() {
        nearbyMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        nearbyMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nearbyMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        nearbyMap.onLowMemory();
    }

}