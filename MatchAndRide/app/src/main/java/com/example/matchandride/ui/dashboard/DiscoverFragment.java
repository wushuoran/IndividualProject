package com.example.matchandride.ui.dashboard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.matchandride.AcceptInvActivity;
import com.example.matchandride.LoginActivity;
import com.example.matchandride.MainActivity;
import com.example.matchandride.R;
import com.example.matchandride.SaveRideActivity;
import com.example.matchandride.SendInvActivity;
import com.example.matchandride.databinding.FragmentDiscoverBinding;
import com.example.matchandride.ui.home.RideFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class DiscoverFragment extends Fragment implements OnMapReadyCallback{

    private DiscoverViewModel discoverViewModel;
    private FragmentDiscoverBinding binding;
    private Switch bikeFilter;
    private Button addList, sendInv;
    private MapView nearbyMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private Marker mylocationMarker;
    private GoogleMap googleMap;
    private LatLng currentLat;
    public static final String TAG = "TAG";
    private HashMap<String, Marker> nearbyUserMap = new HashMap<String, Marker>();
    private boolean rtDbIsSet = false;
    private ArrayList<String> invitedUsers;
    private ValueEventListener invEvnLis;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        discoverViewModel =
                new ViewModelProvider(this).get(DiscoverViewModel.class);

        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        invitedUsers = new ArrayList<>();
        bikeFilter = (Switch) root.findViewById(R.id.switch_filter);
        nearbyMap = (MapView) root.findViewById(R.id.map_nearby);
        sendInv = (Button) root.findViewById(R.id.btn_set_inv);

        nearbyMap.onCreate(savedInstanceState);
        nearbyMap.getMapAsync(this);

        listenToInv();

        setListeners();

        return root;
    }

    public void setListeners(){

        sendInv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mAuth.getCurrentUser() == null)
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                else {
                    if (invitedUsers.size()>0){
                        Intent intent = new Intent(getActivity(), SendInvActivity.class);
                        intent.putExtra("inviteList", invitedUsers);
                        startActivity(intent);
                        getActivity().recreate();
                    }else{
                        Toast.makeText(getActivity().getApplicationContext(), "No user in Invite List!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public void setMap(){

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        else if(ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            this.googleMap.setTrafficEnabled(true);
            this.googleMap.setIndoorEnabled(true);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    currentLat = new LatLng(latitude, longitude);
                    if (mylocationMarker!=null) mylocationMarker.remove();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            // set the center of the map to user's location
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(13)             // Sets the zoom
                            .build();             // Creates a CameraPosition from the builder
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    mylocationMarker = googleMap.addMarker(
                            new MarkerOptions()
                                    .position(currentLat)
                                    .title("Me")
                                    .snippet("20kph 5/5")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_384380_16)));
                    if (MainActivity.onlineSwitchStatus && !MainActivity.isBackground){
                        MainActivity.mDbLoc.child(MainActivity.mAuth.getUid()).setValue(currentLat);
                        MainActivity.mDbLoc.child(MainActivity.mAuth.getUid()).onDisconnect().removeValue();
                        if (!rtDbIsSet) {
                            setNearbyUsers();
                        }
                    }else{
                        if (mylocationMarker!=null) mylocationMarker.remove();
                        mylocationMarker = googleMap.addMarker(
                                new MarkerOptions()
                                        .position(currentLat)
                                        .title("Me")
                                        .snippet("20kph 5/5")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_grey_16)));
                    }

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
        }

    }

    public void setNearbyUsers(){


        if (RideFragment.onlineSwitch.isChecked() && currentLat != null){
            System.out.println("Start finding nearby users...");
            this.rtDbIsSet = true;
            MainActivity.mDbLoc.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot sp : snapshot.getChildren()){ // for each user in the database
                        if (!sp.getKey().equals(MainActivity.mAuth.getCurrentUser().getUid())){ // not user himself
                            System.out.println("User " + sp.getKey() + " found!");
                            LatLng userLoc = new LatLng(Float.valueOf(sp.child("latitude").getValue().toString()), Float.valueOf(sp.child("longitude").getValue().toString()));
                            String nearUserUid = sp.getKey();
                            float[] distanceResult = new float[1]; // distance result will stored in this array
                            Location.distanceBetween(currentLat.latitude, currentLat.longitude, userLoc.latitude, userLoc.longitude, distanceResult);
                            // show users within the distance of 10km, and the user is newly found
                            if (distanceResult[0] <= 5000 && !nearbyUserMap.containsKey(nearUserUid)){
                                System.out.println("User " + sp.getKey() + " is in 10km");
                                // get current nearby user information
                                System.out.println("get current nearby user information");
                                DocumentReference dRef = MainActivity.mStore.collection("UserNames").document(nearUserUid);
                                final String[] nearUserInfo = new String[1];
                                ArrayList<String> testarr = new ArrayList<>();
                                dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                nearUserInfo[0] = document.getString("Username");
                                                System.out.println(nearUserInfo[0]);
                                                /* GET&SET MORE INFO HERE, AVG SPEED, RATING, .... */
                                                testarr.add(nearUserInfo[0]);

                                                if (nearbyUserMap.containsKey(nearUserUid))
                                                    nearbyUserMap.get(nearUserUid).setPosition(userLoc);
                                                else{
                                                    Marker userMk = googleMap.addMarker(new MarkerOptions()
                                                            .position(userLoc)
                                                            .title(nearUserInfo[0])
                                                            .snippet("20kph, 5/5")
                                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_red_16)));
                                                    nearbyUserMap.put(nearUserUid, userMk);
                                                }

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
                            // an existing user within the range 10km
                            else if (distanceResult[0] <= 5000 && nearbyUserMap.containsKey(nearUserUid)){
                                System.out.println("Have found this guy, update his location");
                                Marker userMk = nearbyUserMap.get(nearUserUid);
                                userMk.setPosition(userLoc);
                            }
                            // an existing user goes out of the range, remove it from map
                            else if (distanceResult[0] > 5000 && nearbyUserMap.containsKey(nearUserUid)){
                                System.out.println("A user travels out of range");
                                nearbyUserMap.get(nearUserUid).remove();
                                nearbyUserMap.remove(nearUserUid);
                            }
                        }
                    }
                    // check offline users in nearby user map, remove them
                    ArrayList<String> offLineUser = new ArrayList<>();
                    for (String existingUser : nearbyUserMap.keySet()){
                        boolean isOffline = true;
                        for (DataSnapshot sp : snapshot.getChildren()){
                            if (sp.getKey().equals(existingUser)) isOffline = false;
                        }
                        if (isOffline) offLineUser.add(existingUser);
                    }
                    if (!offLineUser.isEmpty()) System.out.println("find offline user, removing it...");
                    for (String offlineuser : offLineUser) {
                        nearbyUserMap.get(offlineuser).remove();
                        nearbyUserMap.remove(offlineuser);
                    }
                    offLineUser.clear();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (MainActivity.mAuth.getCurrentUser() != null) {
            setMap();
            this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    LatLng markerLoc = marker.getPosition();
                    if (!marker.getTitle().equals("Me") && !markerLoc.equals(currentLat)){
                        float[] distanceResult = new float[1]; // distance result will stored in this array
                        Location.distanceBetween(currentLat.latitude, currentLat.longitude, markerLoc.latitude, markerLoc.longitude, distanceResult);
                        int distanceToMe = (int) (distanceResult[0]/1000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("User AVG speed: " + "\nUser Rating: " + "\nApprox. Distance: " + distanceToMe + "km")
                                .setTitle(marker.getTitle()).setCancelable(true);
                        builder.setPositiveButton("Add To List", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String userUid = findUidByMarker(marker);
                                if (userUid != null && !invitedUsers.contains(userUid)){
                                    invitedUsers.add(userUid);
                                    Toast.makeText(getActivity().getApplicationContext(), (marker.getTitle()+" is added to Invite List"), Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(getActivity().getApplicationContext(), (marker.getTitle()+" already in Invite List!!!"), Toast.LENGTH_SHORT).show();
                                }
                                System.out.println(userUid + " is selected");
                            }
                        });
                        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.cancel();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        }
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

    public String findUidByMarker (Marker marker){
        String targetUid = null;
        for (String uid : this.nearbyUserMap.keySet())
            if (this.nearbyUserMap.get(uid).equals(marker))
                targetUid = uid;
        return targetUid;
    }

    public void listenToInv() {
        if (MainActivity.onlineSwitchStatus) {
            invEvnLis = new ValueEventListener() {
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
                                Toast.makeText(getActivity(), "One Invitation Received !\nview details in Discover page.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getContext(), AcceptInvActivity.class);
                                intent.putExtra("meetPlace", meetPlace);
                                intent.putExtra("senderUid", invSender);
                                intent.putExtra("estParti", estParti);
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

}