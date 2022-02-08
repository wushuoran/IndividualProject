package com.example.myapplication.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.LoginActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentMeBinding;

public class MeFragment extends Fragment {

    private MeViewModel meViewModel;
    private FragmentMeBinding binding;
    Button editPro, manageRide, friReq, friList, accSetting, loginBtn, logoutBtn;
    ImageView portrait;
    TextView userName, avgSpeed, rating;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        meViewModel =
                new ViewModelProvider(this).get(MeViewModel.class);

        binding = FragmentMeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
/*
        final TextView textView = binding.textNotifications;
        meViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

 */

        editPro = (Button) root.findViewById(R.id.btn_edit_profile);
        editPro.setEnabled(MainActivity.loginStatus);
        manageRide = (Button) root.findViewById(R.id.btn_manage_ride);
        friReq = (Button) root.findViewById(R.id.btn_fri_req);
        friReq.setEnabled(MainActivity.loginStatus);
        friList = (Button) root.findViewById(R.id.btn_fri_list);
        friList.setEnabled(MainActivity.loginStatus);
        accSetting = (Button) root.findViewById(R.id.btn_account_set);
        accSetting.setEnabled(MainActivity.loginStatus);
        loginBtn = (Button) root.findViewById(R.id.btn_login);
        loginBtn.setEnabled(!MainActivity.loginStatus);
        logoutBtn = (Button) root.findViewById(R.id.btn_logout);
        logoutBtn.setEnabled(MainActivity.loginStatus);
        portrait = (ImageView) root.findViewById(R.id.img_port);
        userName = (TextView) root.findViewById(R.id.text_username);
        avgSpeed = (TextView) root.findViewById(R.id.text_avg_speed);
        rating = (TextView) root.findViewById(R.id.text_user_rating);
        setListeners();

        return root;
    }

    public void setListeners(){
        OnClick oc = new OnClick();
        loginBtn.setOnClickListener(oc);
    }

    public class OnClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}