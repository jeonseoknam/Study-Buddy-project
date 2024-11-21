package com.example.studybuddy;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.studybuddy.utility.userData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.PrimitiveIterator;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        updateUserData();

//        // TimerService 시작
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Intent serviceIntent = new Intent(this, TimerService.class);
//            startForegroundService(serviceIntent);
//        } else {
//            Intent serviceIntent = new Intent(this, TimerService.class);
//            startService(serviceIntent);
//        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
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
                    transferTo(MyNotificationsFragment.newInstance("param1", "param2"));
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

            }
        });
    }

    private void transferTo(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
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
                        userData.userName = (String) document.getData().get("Name");
                        userData.userEmail = (String) document.getData().get("Email");
                        userData.userPassword = (String) document.getData().get("Password");
                        userData.userSchool = (String) document.getData().get("School");
                        userData.userMajor = (String) document.getData().get("Major");
                        userData.userNickname = (String) document.getData().get("Nickname");
                        userData.profileUrl = (String) document.getData().get("ProfileImage");
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

