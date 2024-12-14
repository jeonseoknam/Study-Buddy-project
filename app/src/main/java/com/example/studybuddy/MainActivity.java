package com.example.studybuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContentInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.PrimitiveIterator;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private SharedPreferences userPref;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        updateUserData();



        // TimerService 시작
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent serviceIntent = new Intent(this, TimerService.class);
            startForegroundService(serviceIntent);
        } else {
            Intent serviceIntent = new Intent(this, TimerService.class);
            startService(serviceIntent);
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (savedInstanceState == null) {
            transferTo(ClassChatListFragment.newInstance("param1", "param2"));
            bottomNavigationView.setSelectedItemId(R.id.chatPage);
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();


                if (itemId == R.id.chatPage) {
                    transferTo(ClassChatListFragment.newInstance("param1", "param2"));
                    return true;
                }

                if (itemId == R.id.timerPage) {
                    transferTo(MyTimerFragment.newInstance("param1", "param2"));
                    return true;
                }

                if (itemId == R.id.notificationPage) {
                    transferTo(new MyNotificationsFragment());
                    return true;
                }

                if (itemId == R.id.personalPage) {
                    transferTo(MyProfileFragment.newInstance("param1", "param2"));
                    return true;
                }

                return false;
            }
        });

        bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.chatPage) {
                    transferTo(ClassChatListFragment.newInstance("param1", "param2"));
                }

            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof ClassChatListFragment ||
            currentFragment instanceof MyTimerFragment ||
            currentFragment instanceof MyNotificationsFragment ||
            currentFragment instanceof MyProfileFragment) {
                AppExitDialogFragment dialogFragment = new AppExitDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), "tag");
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void transferTo(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        String tag = fragment.getClass().getSimpleName();

        // 이미 추가된 프래그먼트가 있는지 확인
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        if (existingFragment != null) {
            // 기존 프래그먼트를 사용
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, existingFragment, tag)
                    .commit();
        } else {
            // 새로운 프래그먼트를 추가
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag)
                    .commit();
        }
    }


    private void updateUserData(){
        FirebaseUser user = mAuth.getCurrentUser();
        DocumentReference docRef = db.collection("userInfo").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("logchk", "Document exists");
                        userPref = getSharedPreferences("userData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = userPref.edit();
                        editor.putString("Name", (String) document.getData().get("Name"));
                        editor.putString("Email", (String)document.getData().get("Email"));
                        editor.putString("Password", (String)document.getData().get("Password"));
                        editor.putString("School", (String)document.getData().get("School"));
                        editor.putString("Major", (String)document.getData().get("Major"));
                        editor.putString("Nickname", (String)document.getData().get("Nickname"));
                        editor.putString("Profile", (String)document.getData().get("ProfileImage"));
                        editor.putString("UID", (String) document.getData().get("UID"));
                        editor.commit();
                    } else {
                        Log.d("logchk", "No such document");
                    }
                } else {
                    Log.d("logchk", "get failed with ", task.getException());
                }
            }
        });
    }
}

