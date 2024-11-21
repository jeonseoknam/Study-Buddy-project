package com.example.studybuddy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.studybuddy.databinding.ActivitySettingBinding;
import com.example.studybuddy.utility.userData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public class SettingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imgUri = null;
    ActivitySettingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.nameText.setText(userData.userName);
        binding.emailAdressText.setText(userData.userEmail);
        binding.schoolNameText.setText(userData.userSchool);
        binding.editMajor.setText(userData.userMajor);
        binding.editNickName.setText(userData.userNickname);

        if (userData.profileUrl != null){
            Glide.with(this).load(userData.profileUrl).into(binding.profileChange);
        }

        binding.profileChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.setAction(Intent.ACTION_PICK);
                imageLauncher.launch(intent);

            }
        });

        binding.btnFinSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMajor = binding.editMajor.getText().toString();
                String newNickname = binding.editNickName.getText().toString();
                String prevPassword = binding.prevPasswordEdit.getText().toString();
                String newPassword = binding.newPasswordEdit.getText().toString();

                if (newMajor.isEmpty()||newMajor == null){
                    Toast.makeText(SettingActivity.this, "잘못된 학과 입력", Toast.LENGTH_SHORT).show();
                } else if (newNickname.isEmpty()||newNickname==null) {
                    Toast.makeText(SettingActivity.this, "잘못된 닉네임 입력", Toast.LENGTH_SHORT).show();
                } else if (!prevPassword.equals(userData.userPassword)){
                    Toast.makeText(SettingActivity.this, "기존 비밀번호가 아닙니다.", Toast.LENGTH_SHORT).show();
                } else if ((!prevPassword.isEmpty()&&!newPassword.isEmpty())&&
                        prevPassword.equals(newPassword)) {
                    Toast.makeText(SettingActivity.this, "기존 비밀번호와 동일합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> data = new HashMap<>();
                    if (!newMajor.equals(userData.userMajor))
                        data.put("Major", newMajor);
                    if (!newNickname.equals(userData.userNickname))
                        data.put("Nickname", newNickname);
              //      if (!prevPassword.isEmpty()&&!newPassword.isEmpty())
              //          data.put("Password", newPassword);
                    db.collection("userInfo").document(mAuth.getCurrentUser().getUid()).update(data);
                    mAuth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SettingActivity.this, "프로필 설정 완료", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


    }

    private ActivityResultLauncher<Intent> imageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == RESULT_OK){
                        Intent intent = o.getData();
                        imgUri = intent.getData();
                        Glide.with(binding.profileChange).load(imgUri).into(binding.profileChange);
                        StorageReference stoRef = storage.getReference("profileImage/" + mAuth.getCurrentUser().getUid());
                        stoRef.putFile(imgUri);
                        stoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                userData.profileUrl = uri.toString();
                                Map<String, Object> data = new HashMap<>();
                                data.put("ProfileImage", userData.profileUrl);
                                db.collection("userInfo").document(mAuth.getCurrentUser().getUid()).update(data);
                            }
                        });
                    }
                }
            }
    );
}