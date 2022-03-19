package com.example.matchandride;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReportRoadConActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView issueLoc;
    private Marker issuePoint;
    private LatLng issueLatLng;
    private EditText issueDetail;
    private Button uploadPic, reportCon;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    private String filename, uploadTime;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_road_con);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();

        issueLoc = (MapView) findViewById(R.id.map_road_issue);
        issueDetail = (EditText) findViewById(R.id.edit_issue);
        uploadPic = (Button) findViewById(R.id.btn_upload_issue_photo);
        reportCon = (Button) findViewById(R.id.btn_report_issue);
        reportCon.setEnabled(false);

        issueLoc.onCreate(savedInstanceState);
        issueLoc.getMapAsync(this);

        setListeners();

    }

    public void setListeners(){

        uploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                startActivityForResult(chooserIntent, 1);
            }
        });

        reportCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String details = issueDetail.getText().toString();
                if (details!=null && issuePoint !=null){
                    if (details.length()>10){
                        GeoPoint geoPoint = new GeoPoint(issueLatLng.latitude, issueLatLng.longitude);
                        Map<String,Object> issueInfo = new HashMap<>();
                        issueInfo.put("Details", details);
                        issueInfo.put("Location", geoPoint);
                        issueInfo.put("PhotoName", filename);
                        issueInfo.put("CreateTime", new Timestamp(0,0).now());
                        mStore.collection("RoadConditions").document(uploadTime).set(issueInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ReportRoadConActivity.this,
                                        "Issue reported, Thank you!", Toast.LENGTH_SHORT).show();
                                RoadConditionActivity.roadCondsAct.recreate();
                                finish();
                            }
                        });
                    }else Toast.makeText(ReportRoadConActivity.this,
                            "Please write more details about the issue", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ReportRoadConActivity.this,
                            "Please make sure issue location is selected and details are entered", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            if (selectedImage != null) {

                // Code for showing progressDialog while uploading
                ProgressDialog progressDialog
                        = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                // Defining the child of storageReference
                // !!!!!Don't worry about multiple files, it will create a new dir over the original one (clear everything) every time you upload a picture
                SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                Date date1 = new Date();
                uploadTime = formatter1.format(date1);
                filename = mAuth.getCurrentUser().getUid() + "-" + uploadTime;
                StorageReference ref = straRef.child("RoadConditions/" + filename);

                // adding listeners on upload
                // or failure of image
                ref.putFile(selectedImage)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Image uploaded successfully
                                        // Dismiss dialog
                                        progressDialog.dismiss();
                                        reportCon.setEnabled(true);
                                        uploadPic.setEnabled(false);
                                        Toast.makeText(ReportRoadConActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                                    }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error, Image not uploaded
                                progressDialog.dismiss();
                                Toast.makeText(ReportRoadConActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            // Progress Listener for loading
                            // percentage on the dialog box
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });
            }
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        double userLat = lastKnownLocation.getLatitude();
        double userLong = lastKnownLocation.getLongitude();
        LatLng myLoc = new LatLng(userLat,userLong);

        googleMap.setMyLocationEnabled(true);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 13));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                // set the center of the map to user's location
                .target(myLoc)
                .zoom(13)             // Sets the zoom
                .build();             // Creates a CameraPosition from the builder
        System.out.println(myLoc);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Click here to pick this place");
                googleMap.clear();
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.addMarker(markerOptions);
            }
        });
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LatLng setLoc = marker.getPosition();
                float[] distanceResult = new float[1]; // distance result will stored in this array
                Location.distanceBetween(setLoc.latitude, setLoc.longitude, userLat, userLong, distanceResult);
                AlertDialog.Builder builder = new AlertDialog.Builder(ReportRoadConActivity.this);
                builder.setMessage("Are you sure to pick this location? \n\nIt cannot be changed once issue is reported.")
                        .setTitle("Confirm Issue Location").setCancelable(true);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        issuePoint = marker;
                        issueLatLng = marker.getPosition();
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

    @Override
    public void onResume() {
        issueLoc.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        issueLoc.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        issueLoc.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        issueLoc.onLowMemory();
    }

}
