package com.example.matchandride;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView timeTotal, disTotal, climbTotal, avgSpd, discardRide, txtDate;
    private MapView traceMap;
    private String dateTime, cloudStoragePath, collectionName;
    final String[] rideInfo = new String[4];
    private ArrayList<LatLng> routePoints;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    private String username;
    public static final String TAG = "TAG";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_del_ride);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();
        try {username = mAuth.getCurrentUser().getUid();}catch (Exception e){}


        if (mAuth.getCurrentUser() != null) {
            try {
                getRideDataFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                getRideDataLocal();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        timeTotal = (TextView) findViewById(R.id.txt_time_final_v);
        disTotal = (TextView) findViewById(R.id.txt_distance_final_v);
        climbTotal = (TextView) findViewById(R.id.txt_climb_final_v);
        avgSpd = (TextView) findViewById(R.id.txt_avg_spd_final_v);
        discardRide = (TextView) findViewById(R.id.txt_del_ride);
        txtDate = (TextView) findViewById(R.id.txt_date);
        traceMap = (MapView) findViewById(R.id.map_record_final_v);


        txtDate.setText(dateTime);
        discardRide.setPaintFlags(discardRide.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        traceMap.onCreate(savedInstanceState);

        setListeners();


    }

    public void setListeners(){

        discardRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewRideActivity.this);
                builder.setMessage("Are you sure to delete this ride?")
                        .setTitle("Confirm").setCancelable(true);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (username != null){
                            mStore.collection(collectionName).document(dateTime).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Document successfully deleted!");
                                }
                            });
                            straRef.child(cloudStoragePath).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "Path File successfully deleted!");
                                }
                            });
                            ManageRideActivity.thisAct.finish(); // force the manage activity to reload
                            startActivity(new Intent(ViewRideActivity.this, ManageRideActivity.class));
                            finish();
                        }else{
                            File fileInfo = new File(getFilesDir(),dateTime+"info");
                            boolean infoDeleted = fileInfo.delete();
                            File fileRoute = new File(getFilesDir(),dateTime+"route");
                            boolean routeDeleted = fileRoute.delete();
                            if (infoDeleted && routeDeleted){
                                ManageRideActivity.thisAct.finish(); // force the manage activity to reload
                                startActivity(new Intent(ViewRideActivity.this, ManageRideActivity.class));
                                finish();
                            }else{
                                Toast.makeText(ViewRideActivity.this, "Delete Failed!!", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    public void getRideDataFirebase() throws IOException {
        Bundle extras = this.getIntent().getExtras();
        this.dateTime = (String) extras.get("dateTime");
        this.collectionName = "Rides-"+this.username;
        DocumentReference dRef = mStore.collection(collectionName).document(dateTime);
        // get ride info
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    rideInfo[0] = document.get("Duration").toString();
                    rideInfo[1] = document.get("Distance").toString();
                    rideInfo[2] = document.get("Climb").toString();
                    rideInfo[3] = document.get("AVGspd").toString();
                    timeTotal.setText(rideInfo[0]);
                    disTotal.setText(rideInfo[1]);
                    climbTotal.setText(rideInfo[2]);
                    avgSpd.setText(rideInfo[3]);
                    Log.d(TAG, "Cached document data: ");
                } else {
                    Log.d(TAG, "Cached get failed: ", task.getException());
                }
            }
        });
        // get location (route) file
        this.cloudStoragePath = "UserRideHistory/"+ username + "_" + dateTime + ".csv";
        System.out.println(cloudStoragePath);
        File tempFile = File.createTempFile("locations", "csv");
        straRef.child(cloudStoragePath).getFile(tempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Location File Obtained");
                try {getLatLngFromCSV(tempFile);} catch(Exception e){e.printStackTrace();}
            }
        });

    }

    public void getRideDataLocal() throws IOException {
        Bundle extras = this.getIntent().getExtras();
        this.dateTime = (String) extras.get("dateTime");
        String targetFileInfo = this.dateTime + "info";
        String targetFileRoute = this.dateTime + "route";
        File[] fileList = getFilesDir().listFiles();
        for (File file : fileList){
            if (file.getName().equals(targetFileInfo)){
                try{
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while((line = br.readLine()) != null){
                        String[] splitLine = line.split(":");
                        if (splitLine[0].contains("Duration")) timeTotal.setText(splitLine[1]);
                        if (splitLine[0].contains("Distance")) disTotal.setText(splitLine[1]);
                        if (splitLine[0].contains("Climb")) climbTotal.setText(splitLine[1]);
                        if (splitLine[0].contains("AVGspd")) avgSpd.setText(splitLine[1]);
                    }
                    br.close();
                }catch(Exception e){e.printStackTrace();}
            }
            if (file.getName().equals(targetFileRoute)){
                Log.d(TAG, "Location File Obtained");
                try {getLatLngFromCSV(file);} catch(Exception e){e.printStackTrace();}
            }
        }
    }

    public void getLatLngFromCSV(File file) throws Exception{
        System.out.println("Start reading csv........");
        routePoints = new ArrayList<>();
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while((line = br.readLine())!=null){
            String[] splitLine = line.split(",");
            routePoints.add(new LatLng(Double.parseDouble(splitLine[0]),Double.parseDouble(splitLine[1])));
        }
        br.close();
        fr.close();
        System.out.println("Finish reading csv!!!!!");
        traceMap.getMapAsync(this);
        System.out.println("Show Path on Map");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Polyline route = googleMap.addPolyline(new PolylineOptions());
        if (!routePoints.isEmpty()){
            route.setPoints(routePoints);
            // zoom the map to some suitable ratio (to fit the whole path)
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng latLngPoint : routePoints)
                boundsBuilder.include(latLngPoint);
            int routePadding = 100;
            LatLngBounds latLngBounds = boundsBuilder.build();
            try {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
            }catch (Exception e){e.printStackTrace();}
        }
    }

    @Override
    public void onResume() {
        traceMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        traceMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        traceMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        traceMap.onLowMemory();
    }

}
