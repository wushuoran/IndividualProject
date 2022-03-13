package com.example.matchandride;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.matchandride.objects.RideObject;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView timeTotal, disTotal, climbTotal, avgSpd, discardRide, txtDate;
    private String timeMills, distance, climb, avgspd;
    private MapView traceMap;
    private String dateTime, cloudStoragePath, collectionName;
    private LinearLayout btns;
    private ViewGroup.MarginLayoutParams params;
    final String[] rideInfo = new String[4];
    private ArrayList<LatLng> routePoints;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    private String username;
    private String isLocal;
    public static final String TAG = "TAG";
    private String[] localRideInfo = new String[4];
    private String tstamp;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_del_ride);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();
        try {username = mAuth.getCurrentUser().getUid();}catch (Exception e){}

        btns = (LinearLayout) findViewById(R.id.linear_layout_btns);
        params = (ViewGroup.MarginLayoutParams) btns.getLayoutParams();
        timeTotal = (TextView) findViewById(R.id.txt_time_final_v);
        disTotal = (TextView) findViewById(R.id.txt_distance_final_v);
        climbTotal = (TextView) findViewById(R.id.txt_climb_final_v);
        avgSpd = (TextView) findViewById(R.id.txt_avg_spd_final_v);
        discardRide = (TextView) findViewById(R.id.txt_del_ride);
        txtDate = (TextView) findViewById(R.id.txt_date);
        traceMap = (MapView) findViewById(R.id.map_record_final_v);

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
                        if (username != null && isLocal.equals("cloud")){
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
                            updateHisAvgSpeed();
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
        this.isLocal = (String) extras.get("isLocal");
        if (this.isLocal.equals("cloud")){
            this.collectionName = "Rides-"+this.username;
            DocumentReference dRef = mStore.collection(collectionName).document(dateTime);
            // get ride info
            dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        try{
                            rideInfo[0] = document.get("Duration").toString();
                            rideInfo[1] = document.get("Distance").toString();
                            rideInfo[2] = document.get("Climb").toString();
                            rideInfo[3] = document.get("AVGspd").toString();
                            timeTotal.setText(rideInfo[0]);
                            disTotal.setText(rideInfo[1]);
                            climbTotal.setText(rideInfo[2]);
                            avgSpd.setText(rideInfo[3]);
                            Log.d(TAG, "Cached document data: ");
                        }catch(NullPointerException e){e.printStackTrace();}

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
        }else{
            getRideDataLocal();
        }

    }

    public void getRideDataLocal() throws IOException {
        Bundle extras = this.getIntent().getExtras();
        this.dateTime = (String) extras.get("dateTime");
        this.isLocal = (String) extras.get("isLocal");
        String targetFileInfo = this.dateTime + "info";
        String targetFileRoute = this.dateTime + "route";
        File[] fileList = getFilesDir().listFiles();
        for (File file : fileList){
            if (file.getName().equals(targetFileInfo)){
                try{
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    for(int i = 0; i<5; i++){
                        System.out.println("read current info file");
                        String line = br.readLine();
                        System.out.println(line);
                        String[] splitLine = line.split(":");
                        String value = splitLine[1];
                        if (line.contains("Duration")){
                            String timepassed = "time";
                            for (int a=1; a<splitLine.length; a++){
                                if (a==1) timepassed = splitLine[a];
                                else timepassed = timepassed + ":" + splitLine[a];
                            }
                            this.timeMills = timepassed;
                        }
                        if (line.contains("Distance")) {
                            this.distance = value;
                        }
                        if (line.contains("Climb")) {
                            this.climb = value;
                        }
                        if (line.contains("AVGspd")) {
                            this.avgspd = value;
                        }
                        if (line.contains("Timestamp")) {this.tstamp = value; } //Timestamp(seconds=1647213096, nanoseconds=274000000)
                    }
                    br.close();
                    this.timeTotal.setText(this.timeMills);
                    this.disTotal.setText(this.distance);
                    this.climbTotal.setText(this.climb);
                    this.avgSpd.setText(this.avgspd);
                }catch(Exception e){e.printStackTrace();
                System.out.println("read info file failed");}
            }
            if (file.getName().equals(targetFileRoute)){
                Log.d(TAG, "Location File Obtained");
                try {getLatLngFromCSV(file);} catch(Exception e){e.printStackTrace();}
            }
        }
        if (mAuth.getCurrentUser() != null){ // add the upload button
            Button tv = new Button(getApplicationContext());
            tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            tv.setHeight((int) ((int) 65*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f));
            params.topMargin = (int) ((int) 5*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f);
            tv.setLayoutParams(params);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(20);
            tv.setGravity(Gravity.CENTER);
            tv.setText("Upload Ride");
            tv.setBackground(getResources().getDrawable(R.drawable.chart_bound1));
            btns.addView(tv);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (uploadToFirebase()) {
                        Toast.makeText(ViewRideActivity.this, "Ride Uploaded", Toast.LENGTH_SHORT).show();
                        File fileInfo = new File(getFilesDir(),dateTime+"info");
                        boolean infoDeleted = fileInfo.delete();
                        File fileRoute = new File(getFilesDir(),dateTime+"route");
                        boolean routeDeleted = fileRoute.delete();
                        if (infoDeleted && routeDeleted){
                            ManageRideActivity.thisAct.finish(); // force the manage activity to reload
                            startActivity(new Intent(ViewRideActivity.this, ManageRideActivity.class));
                            finish();
                        }else{
                            Toast.makeText(ViewRideActivity.this, "Local File Delete Failed!!", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ViewRideActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }

    public boolean uploadToFirebase(){
        AtomicBoolean uploadResult = new AtomicBoolean(true);
        Map<String, Object> rideInfo = new HashMap<>();
        rideInfo.put("Duration", this.timeMills);
        rideInfo.put("Distance", this.distance);
        rideInfo.put("Climb", this.climb);
        rideInfo.put("AVGspd", this.avgspd);
        rideInfo.put("Timestamp", getTimestampFromStr(this.tstamp));
        // store ride basic info into Firebase
        mStore.collection("Rides-" + mAuth.getCurrentUser().getUid())
                .document(dateTime).set(rideInfo)
                .addOnCompleteListener((OnCompleteListener<Void>) (aVoid) -> {
                    Log.d(TAG, "DocumentSnapshot added");
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
                uploadResult.set(false);
            }
        });
        // store ride path file into Firebase
        try {
            String filename = mAuth.getCurrentUser().getUid() + "_" + dateTime + ".csv"; // file name
            File file = new File(this.getBaseContext().getFilesDir(), filename);   // should write to internal storage first
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            if (!routePoints.isEmpty()) {
                for (LatLng point : routePoints) {
                    bw.write(point.latitude + "," + point.longitude);
                    bw.newLine();
                }
                bw.close();
                fw.close();
                StorageReference ref = straRef.child("UserRideHistory/" + filename);
                Uri fromFile = Uri.fromFile(file);
                ref.putFile(fromFile).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ViewRideActivity.this, "Route Upload FAILED", Toast.LENGTH_SHORT).show();
                        uploadResult.set(false);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadResult.get();
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

    public void updateHisAvgSpeed(){

        String collectionName = "Rides-" + mAuth.getCurrentUser().getUid();

        mStore.collection(collectionName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ArrayList<Double> allAVGspd = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()){
                        RideObject ro = document.toObject(RideObject.class);
                        double avgspd = 0;
                        try {
                            avgspd = ro.getAVGspd();
                        }catch (Exception e){e.printStackTrace();}
                        System.out.println("AVG spd got hahahahah" + avgspd);
                        allAVGspd.add(avgspd);
                        if (!allAVGspd.isEmpty()){
                            double sum = 0;
                            for (Double d : allAVGspd) sum = sum + d;
                            Double hisAVGspd = (double) Math.round((sum / allAVGspd.size()) * 10) / 10 ;
                            System.out.println("history avg spd calculated jajajajaja" + hisAVGspd);
                            Map<String, Object> updateInfo = new HashMap<String, Object>();
                            updateInfo.put("AVGspd", hisAVGspd);
                            mStore.collection("UserNames").document(mAuth.getCurrentUser().getUid()).update(updateInfo);
                            System.out.println("his avg spd updated wahoooooo");
                        }
                    }
                }else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    public Timestamp getTimestampFromStr(String ts){ //Timestamp(seconds=1647213096, nanoseconds=274000000)
        ArrayList<String> matchList = new ArrayList<String>();
        Pattern regex = Pattern.compile("\\((.*?)\\)");
        Matcher regexMatcher = regex.matcher(ts);

        while (regexMatcher.find()) {//Finds Matching Pattern in String
            matchList.add(regexMatcher.group(1));//Fetching Group from String
        }

        String tsArg = matchList.get(0);
        String[] split1 = tsArg.split(",");
        String splitSec = split1[0].split("=")[1];
        String splitNan = split1[1].split("=")[1];
        long seconds = 0; int nano = 0;
        try {
            seconds = (long) Integer.valueOf(splitSec);
            nano = Integer.valueOf(splitNan);
        }catch (Exception e){e.printStackTrace();}
        return new Timestamp(seconds,nano);
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
