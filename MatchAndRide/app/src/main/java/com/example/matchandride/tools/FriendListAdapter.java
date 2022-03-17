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

import com.example.matchandride.FindFriendsActivity;
import com.example.matchandride.R;
import com.example.matchandride.ViewFriendsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FriendListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater li;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    private HashMap<Integer, String> friends;

    public FriendListAdapter(Context context, HashMap<Integer, String> friends){
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();
        this.context = context;
        this.li = LayoutInflater.from(context);
        this.friends = new HashMap<>();
        this.friends = friends;
        System.out.println(friends);
    }

    static class ViewHolder {
        public ImageView imageView;
        public TextView itemUsername, itemUserinfo;
        public Button btn;
    }

    @Override
    public int getCount() {
        return friends.size();
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
            view = li.inflate(R.layout.item_user_info,null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.iv_portrait_user);
            holder.itemUsername = (TextView) view.findViewById(R.id.tv_item_username);
            holder.itemUserinfo = (TextView) view.findViewById(R.id.tv_item_userinfo);
            holder.btn = (Button) view.findViewById(R.id.btn_add_acc);
            holder.btn.setText("Delete");
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        String uid = friends.get(i);
        final ImageView finalIV = holder.imageView;
        final TextView finalTV = holder.itemUsername;
        final TextView finalTV2 = holder.itemUserinfo;
        // try to set current group member's profile picture
        try{
            String cloudStoragePath = "UserProfilePics/" + uid;
            File localFile = File.createTempFile("images","jpg");
            straRef.child(cloudStoragePath).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    try{finalIV.setImageURI(Uri.parse(localFile.toString()));}catch (NullPointerException e){}
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
                String info = task.getResult().get("AVGspd") + "kph, " + task.getResult().get("Rating") + "/5";
                finalTV2.setText(info);
            }
        });
        holder.itemUsername.setText(finalTV.getText());
        holder.itemUserinfo.setText(finalTV2.getText());
        final Button btn2 = holder.btn;
        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewFriendsActivity.viewFri);
                builder.setMessage("Delete " + finalTV.getText() + "?").setTitle("Confirm").setCancelable(true);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Map<String, Object> friendDel = new HashMap<>();
                        friendDel.put(mAuth.getCurrentUser().getUid(), FieldValue.delete()); // remove current user in target user's list
                        mStore.collection("UserFriends").document(uid).update(friendDel);
                        Map<String, Object> friendDel2 = new HashMap<>();
                        friendDel2.put(uid, FieldValue.delete()); // remove the target user in current user's list
                        mStore.collection("UserFriends").document(mAuth.getCurrentUser().getUid()).update(friendDel2);
                        btn2.setEnabled(false);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        holder.btn.setEnabled(btn2.isEnabled());
        return view;
    }

}
