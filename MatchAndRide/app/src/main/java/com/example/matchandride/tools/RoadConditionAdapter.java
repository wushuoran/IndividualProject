package com.example.matchandride.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.matchandride.MainActivity;
import com.example.matchandride.R;
import com.example.matchandride.ReportRoadConActivity;
import com.example.matchandride.RoadConditionActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RoadConditionAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater li;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    private HashMap<Integer, String> issueLocs, issuePhos;

    public RoadConditionAdapter(Context context, HashMap<Integer, String> issueLocs, HashMap<Integer, String> issuePhos){
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();
        this.context = context;
        this.li = LayoutInflater.from(context);
        this.issueLocs = new HashMap<>();
        this.issuePhos = new HashMap<>();
        this.issuePhos = issuePhos;
        this.issueLocs = issueLocs;
        System.out.println(issueLocs);
    }

    static class ViewHolder {
        public ImageView imageView;
        public TextView details;
        public Button btn;
    }

    @Override
    public int getCount() {
        return issueLocs.size();
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
            view = li.inflate(R.layout.item_road_cond,null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.iv_road_pic);
            holder.details = (TextView) view.findViewById(R.id.tv_issue_details);
            holder.btn = (Button) view.findViewById(R.id.btn_report_solved);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        try {
            String photoName = issuePhos.get(i);
            String cloudStoragePath = "RoadConditions/" + photoName;
            File localFile = File.createTempFile("images","jpg");
            final ImageView iv = holder.imageView;
            straRef.child(cloudStoragePath).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    try {
                        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.straRef..getContentResolver(), uri);
                        iv.setImageURI(Uri.parse(localFile.toString()));
                        //System.out.println("Profile Picture Updated!!!!!!!!!!!!");
                        //Toast.makeText(getActivity(), "Profile Picture Updated!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
            holder.imageView = iv;
        }catch (Exception e){}
        final TextView tv = holder.details;
        mStore.collection("RoadConditions").document(issueLocs.get(i)).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String issDetail = task.getResult().get("Details").toString();
                        tv.setText(issDetail);
                    }
                });
        holder.details = tv;
        final Button btn2 = holder.btn;
        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RoadConditionActivity.roadCondsAct);
                builder.setMessage("Please confirm this issue solved")
                        .setTitle("Confirm Issue Resolved").setCancelable(true);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Map<String, Object> reportSolved = new HashMap<>();
                        reportSolved.put(mAuth.getCurrentUser().getUid(), true);
                        mStore.collection("RoadConditions").document(issueLocs.get(i)).set(reportSolved, SetOptions.merge());
                        mStore.collection("RoadConditions").document(issueLocs.get(i)).get().addOnCompleteListener(
                                new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        Map<String, Object> issueInfo = task.getResult().getData();
                                        if (issueInfo!=null){
                                            if (issueInfo.size() > 6) { // over 2 people report the issue is solved
                                                mStore.collection("RoadConditions").document(issueLocs.get(i)).delete();
                                                String photoName = issuePhos.get(i);
                                                String cloudStoragePath = "RoadConditions/" + photoName;
                                                straRef.child(cloudStoragePath).delete();
                                            }
                                        }
                                    }
                                }
                        );
                        /*if (btn2.getText().equals("Have Solved")) mStore.collection("RoadConditions").document(issueLocs.get(i)).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(RoadConditionActivity.roadCondsAct,
                                                "This issue has reported solved by many users, now mark as solved", Toast.LENGTH_SHORT).show();
                                    }
                                });*/
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
        return view;
    }
}
