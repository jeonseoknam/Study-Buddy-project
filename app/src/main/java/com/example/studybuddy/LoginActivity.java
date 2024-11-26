package com.example.studybuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studybuddy.utility.userData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();

        Button loginButton = findViewById(R.id.btn_Login);
        loginButton.setOnClickListener(v -> performLogin());
        //회원가입 화면으로 이동
        TextView gotoSignup = findViewById(R.id.toSignUpText);
        gotoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    //로그인 유지용
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            updateUI(user);
        }
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.d("logchk", "updateUI: " + user.getUid());
           // db.collection("userInfo");
         /*   docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("logchk", "userData saved");
                            userPref = getSharedPreferences("userData", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = userPref.edit();
                            editor.putString("Name", document.getData().get("Name").toString());
                            editor.putString("Email", document.getData().get("Email").toString());
                            editor.putString("Password", document.getData().get("Password").toString());
                            editor.putString("School", document.getData().get("School").toString());
                            editor.putString("Major", document.getData().get("Major").toString());
                            editor.putString("Nickname", document.getData().get("Nickname").toString());
                            editor.putString("Profile", document.getData().get("ProfileImage").toString());
                            editor.apply();
                        } else {
                            Log.d("logchk", "No such document");
                        }
                    } else {
                        Log.d("logchk", "get failed with ", task.getException());
                    }
                }
            });*/
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
    private void performLogin() {
        String email = ((EditText) findViewById(R.id.editTextEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                        //메인으로 이동
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}