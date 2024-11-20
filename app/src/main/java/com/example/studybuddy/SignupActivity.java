package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String name, school, nickname, email, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance(); // FirebaseAuth 초기화
        db = FirebaseFirestore.getInstance();
        name = null; school = null; nickname = null; email = null; password = null;

        Button signUpButton = findViewById(R.id.btn_signUp);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSignUp();
            }
        });
    }

    private void performSignUp() {
        name = ((EditText)findViewById(R.id.editTextUsername)).getText().toString();
        school = ((EditText)findViewById(R.id.editTextSchoolName)).getText().toString();
        nickname = ((EditText)findViewById(R.id.editTextNickName)).getText().toString();
        email = ((EditText)findViewById(R.id.editTextEmail2)).getText().toString();
        password = ((EditText) findViewById(R.id.editTextPassword2)).getText().toString();

        HashMap<String , Object> userdata = new HashMap<>();
        userdata.put("Name", name);
        userdata.put("School", school);
        userdata.put("Nickname", nickname);
        userdata.put("Email", email);
        userdata.put("Password", password);

        if ((name != null && !name.isEmpty()) &&
                (school != null && !school.isEmpty()) &&
                (nickname != null && !nickname.isEmpty()) &&
                (email != null && !email.isEmpty()) &&
                (password != null && !password.isEmpty()) )
        {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            userdata.put("UID", user.getUid());
                            userdata.put("ProfileImage", "none");
                            db.collection("userInfo").document(user.getUid()).set(userdata);
                            Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();

                            // 추가적인 정보 저장을 원한다면 Firebase Realtime Database 또는 Firestore를 사용
                        } else {
                            Toast.makeText(this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "다시 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

}