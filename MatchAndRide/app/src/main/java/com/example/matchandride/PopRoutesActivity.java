package com.example.matchandride;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.matchandride.tools.PopRoutesAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PopRoutesActivity extends AppCompatActivity implements OnMapReadyCallback{

    //private ListView lvPop;
    public static Activity popRA;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    private ArrayList<LatLng> routePoints;
    private MapView traceMap;
    private TextView routeInfo;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_routes);

        popRA = this;

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();

        //lvPop = (ListView) findViewById(R.id.lv_pop_routes);
        //lvPop.setAdapter(new PopRoutesAdapter(PopRoutesActivity.this, savedInstanceState));

        traceMap = (MapView) findViewById(R.id.map_pop_route);
        routeInfo = (TextView) findViewById(R.id.tv_route_info);
        routePoints = new ArrayList<>();


        String cloudStoragePath  = "UserRideHistory/v8i2BnJT1UYpwIsFnsMp8uVuaEs2_19-02-2022 19:57.csv";
        System.out.println(cloudStoragePath);

        try {
            File tempFile = File.createTempFile("locations", "csv");
            straRef.child(cloudStoragePath).getFile(tempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    try {getLatLngFromCSV(tempFile, traceMap);} catch(Exception e){e.printStackTrace();}
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        traceMap.onCreate(savedInstanceState);


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

    public void getLatLngFromCSV(File file, MapView traceMap) throws Exception{
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
