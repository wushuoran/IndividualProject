package com.example.matchandride.tools;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.matchandride.MainActivity;
import com.example.matchandride.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.snapshot.Index;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RateUsersAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater li;
    private ArrayList<String> groupMembers;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    private HashMap<Integer, String> memberIndex;

    public RateUsersAdapter(Context context, ArrayList<String> groupMembers){
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();
        this.context = context;
        this.li = LayoutInflater.from(context);
        this.groupMembers = groupMembers;
        this.memberIndex = new HashMap<>();
        int index = 0;
        for (String uid: groupMembers){
            if (!uid.equals(mAuth.getCurrentUser().getUid())){
                memberIndex.put(index, uid);
                index++;
            }
        }
    }

    @Override
    public int getCount() {
        return memberIndex.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    static class ViewHolder {
        public ImageView imageView;
        public TextView rateUsername;
        public RadioGroup ratings;
    }

    // NOTE i in the parameters, make sure index in hashmap starts from 0, this i starts from 0
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null){
            view = li.inflate(R.layout.item_rate_users,null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.iv_rate_user);
            holder.rateUsername = (TextView) view.findViewById(R.id.tv_rate_username);
            holder.ratings = (RadioGroup) view.findViewById(R.id.radio_group_ratings);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        String uid = memberIndex.get(i);
        final ImageView finalIV = holder.imageView;
        final TextView finalTV = holder.rateUsername;
        // try to set current group member's profile picture
        try{
            String cloudStoragePath = "UserProfilePics/" + uid;
            File localFile = File.createTempFile("images","jpg");
            straRef.child(cloudStoragePath).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    finalIV.setImageURI(Uri.parse(localFile.toString()));
                }
            });
        }catch (Exception e){e.printStackTrace();}
        holder.imageView = finalIV;
        // try to set current group member's username
        mStore.collection("UserNames").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                    finalTV.setText(task.getResult().get("Username").toString());
            }
        });
        holder.rateUsername.setText(finalTV.getText());

        Map<String,Object> rating = new HashMap<>();
        rating.put(mAuth.getCurrentUser().getUid(), 5);
        mStore.collection("UserRatings").document(uid).set(rating, SetOptions.merge());

        holder.ratings.check(R.id.cb_rate_5);
        holder.ratings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Map<String,Object> rating = new HashMap<>();
                RadioButton rb = radioGroup.findViewById(i);
                rating.put(mAuth.getCurrentUser().getUid(), Integer.valueOf(rb.getText().toString()));
                mStore.collection("UserRatings").document(uid).set(rating, SetOptions.merge());
            }
        });

        return view;
    }



}
