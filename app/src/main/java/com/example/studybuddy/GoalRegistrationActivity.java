package com.example.studybuddy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studybuddy.databinding.ActivityGoalRegistrationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GoalRegistrationActivity extends AppCompatActivity {

    private ActivityGoalRegistrationBinding binding;
    private String selectedDate = "";
    private long daysUntilDue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoalRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        binding.btnRegisterGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = binding.editGoalTitle.getText().toString();
                String description = binding.editGoalDescription.getText().toString();

                if (title.isEmpty() || description.isEmpty() || selectedDate.isEmpty()) {
                    Toast.makeText(GoalRegistrationActivity.this, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    saveGoalToFirestore(title, description, daysUntilDue);
                }
            }
        });

    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
                selectedDate = dateFormat.format(selectedCalendar.getTime());
                binding.textSelectedDate.setText(selectedDate);

                // 현재 날짜와 선택한 날짜 간의 D-Day 계산
                Calendar currentCalendar = Calendar.getInstance();
                long diffInMillis = selectedCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();
                daysUntilDue = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            }
        }, year, month, date);
        datePickerDialog.show();
    }



    private void saveGoalToFirestore(String title, String description, long dueInDays) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String chatRoomId = getIntent().getStringExtra("chatRoomId"); // Intent로 전달받은 chatRoomId

        if (chatRoomId == null || chatRoomId.isEmpty()) {
            Log.e("logchk", "ChatRoomId is null or empty. Goal cannot be saved.");
            Toast.makeText(this, "ChatRoomId가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> goalData = new HashMap<>();
        goalData.put("title", title);
        goalData.put("description", description);
        goalData.put("dueInDays", dueInDays);
        goalData.put("isCertified", false);
        goalData.put("userId", userId);

        // chatRoomId 기반 경로로 데이터 저장
        db.collection("Goals").document(chatRoomId).collection("goals")
                .add(goalData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "목표가 등록되었습니다!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish(); // Activity 종료
                })
                .addOnFailureListener(e -> Log.e("logchk", "Error Adding Goal: " + e.getMessage()));
    }

}