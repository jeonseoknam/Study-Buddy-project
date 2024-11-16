package com.example.studybuddy;

import android.os.Bundle;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}

