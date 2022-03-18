package com.example.matchandride.tools;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.matchandride.PopRoutesActivity;
import com.example.matchandride.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PopRoutesAdapter /*extends BaseAdapter implements OnMapReadyCallback*/ {

    private Context context;
    private LayoutInflater li;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    private ArrayList<LatLng> routePoints;
    private Bundle savedInstanceState;
    public static MapView traceMap2;

    public PopRoutesAdapter(Context context, Bundle savedInstanceState){
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();
        this.context = context;
        this.li = LayoutInflater.from(context);
        this.savedInstanceState = savedInstanceState;
    }
/*
    static class ViewHolder{
        public MapView popRoute;
        public TextView routeInfo;
    }

    @Override
    public int getCount() {
        return 2; // display some sample routes
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null){
            view = li.inflate(R.layout.item_pop_routes,null);
            holder = new ViewHolder();
            holder.popRoute = (MapView) view.findViewById(R.id.map_pop_route);
            holder.routeInfo = (TextView) view.findViewById(R.id.tv_route_info);
            routePoints = new ArrayList<>();
            MapsInitializer.initialize(context);
            // get location (route) file
            String cloudStoragePath = null;
            if (i==0)
                cloudStoragePath = "UserRideHistory/v8i2BnJT1UYpwIsFnsMp8uVuaEs2_19-02-2022 19:57.csv";
            if (i==1)
                cloudStoragePath = "UserRideHistory/v8i2BnJT1UYpwIsFnsMp8uVuaEs2_21-02-2022 16:59.csv";
            System.out.println(cloudStoragePath);
            try{
                File tempFile = File.createTempFile("locations", "csv");
                straRef.child(cloudStoragePath).getFile(tempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) { }});
                try {getLatLngFromCSV(tempFile, holder.popRoute);} catch(Exception e){e.printStackTrace();}
            }catch (Exception e){}
            traceMap2 = holder.popRoute;
        }else{
            holder = (ViewHolder) view.getTag();
        }
        return view;
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
        traceMap.onCreate(savedInstanceState);
        traceMap.getMapAsync(this);

        System.out.println("Show Path on Map");
    }

*/
}
