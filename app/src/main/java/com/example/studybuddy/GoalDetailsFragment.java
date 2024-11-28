package com.example.studybuddy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class GoalDetailsFragment extends Fragment {

    private static final String TAG = "GoalDetailsFragment";
    private static final String GOAL_ID_KEY = "goalId";
    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseFirestore db;
    private String goalId;
    private ListenerRegistration goalListener;

    private TextView goalTitleTextView;
    private TextView goalDescriptionTextView;
    private TextView goalDueDateTextView;
    private Button actionButton;
    private Button addImageButton;
    private ImageView certificationImagePreview;
    private EditText certificationDescription;

    private TextView chatTitleTextView;
    private TextView certificationDescriptionSet;
    private Uri imageUri;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal_details, container, false);

        db = FirebaseFirestore.getInstance();

        // UI 요소 초기화

        chatTitleTextView = view.findViewById(R.id.chatRoomNameText);
        String chatRoomId = getArguments().getString("chatRoomId");
        chatTitleTextView.setText(chatRoomId);


        goalTitleTextView = view.findViewById(R.id.goalDetailsTitle);
        goalDescriptionTextView = view.findViewById(R.id.goalDetailsDescription);
        goalDueDateTextView = view.findViewById(R.id.goalDetailsDueDate);
        actionButton = view.findViewById(R.id.btn_goalDetailsChange);
        addImageButton = view.findViewById(R.id.btn_addImage);  // 이미지 추가 버튼
        certificationImagePreview = view.findViewById(R.id.certificationImagePreview);
        certificationDescription = view.findViewById(R.id.certificationDescription);
        certificationDescriptionSet = view.findViewById(R.id.certificationDescriptionSet);

        // Fragment에 전달된 goalId 가져오기
        if (getArguments() != null) {
            goalId = getArguments().getString(GOAL_ID_KEY);
        }

        if (goalId != null) {
            loadGoalDetails(goalId);
        } else {
            Log.e(TAG, "Goal ID is null");
        }

        // 이미지 추가 버튼 클릭 시 파일 선택
        addImageButton.setOnClickListener(v -> openFileChooser());

        // 인증 버튼 클릭 시 인증 처리
        actionButton.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToFirebase(); // 이미지 업로드 후 Firestore 업데이트
            } else {
                Toast.makeText(getContext(), "이미지를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (goalListener != null) {
            goalListener.remove();
        }
    }

    private void loadGoalDetails(String goalId) {
        String chatRoomId = getArguments().getString("chatRoomId"); // 전달받은 chatRoomId

        if (chatRoomId == null || chatRoomId.isEmpty()) {
            Log.e(TAG, "ChatRoomId is missing!");
            Toast.makeText(getContext(), "ChatRoomId가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firestore 경로에 chatRoomId를 포함
        DocumentReference goalRef = db.collection("Goals")
                .document(chatRoomId)
                .collection("goals")
                .document(goalId);

        goalListener = goalRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e(TAG, "Error fetching goal details: " + error.getMessage());
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // 기본값 설정
                String status = snapshot.getString("status");
                if (status == null) {
                    status = "pending"; // 기본값 설정
                    goalRef.update("status", "pending") // Firestore에도 기본값 저장
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update status: " + e.getMessage()));
                }

                updateUI(snapshot);
            }
        });
    }
    private void updateUI(DocumentSnapshot snapshot) {
        String title = snapshot.getString("title");
        String description = snapshot.getString("description");
        long dueInDays = snapshot.getLong("dueInDays") != null ? snapshot.getLong("dueInDays") : 0;
        String userId = snapshot.getString("userId");
        String certificationImageUrl = snapshot.getString("certificationImageUrl");
        String certificationDescriptionText = snapshot.getString("certificationDescription");
        String status = snapshot.getString("status");

        goalTitleTextView.setText(title);
        goalDescriptionTextView.setText(description);
        goalDueDateTextView.setText("D-Day: " + dueInDays);

        if (certificationImageUrl != null && !certificationImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(certificationImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(certificationImagePreview);
        } else {
            certificationImagePreview.setImageResource(R.drawable.placeholder_image);
        }

        if (certificationDescriptionText != null && !certificationDescriptionText.isEmpty()) {
            certificationDescriptionSet.setText(certificationDescriptionText);
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 버튼 로직
        if (userId != null && userId.equals(currentUserId)) {
            // 본인: 인증 버튼만 표시
            if ("pending".equals(status)) {
                actionButton.setText("인증하기");
                actionButton.setOnClickListener(v -> {
                    if (imageUri != null) {
                        uploadImageToFirebase();
                    } else {
                        Toast.makeText(getContext(), "이미지를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // 본인은 인증 후 버튼 숨김
                actionButton.setVisibility(View.GONE);
            }
        } else {
            // 타인: 인정 버튼만 표시
            actionButton.setText("인정하기");

            // 인증 상태에 따라 버튼 동작 설정
            actionButton.setOnClickListener(v -> {
                if ("pending".equals(status)) {
                    Toast.makeText(getContext(), "아직 인증이 되지 않았습니다!", Toast.LENGTH_SHORT).show();
                } else if ("certified".equals(status)) {
                    endorseGoal(goalId); // 목표 인정 로직 호출
                }
            });
        }
    }



    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            certificationImagePreview.setImageURI(imageUri);
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("certifications").child(System.currentTimeMillis() + ".jpg");

            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveCertificationToFirestore(imageUrl); // 이미지 URL 획득 후 Firestore 업데이트
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed: " + e.getMessage());
            });
        }
    }

    private void saveCertificationToFirestore(String imageUrl) {
        String description = certificationDescription.getText().toString();
        String chatRoomId = getArguments().getString("chatRoomId"); // 전달받은 chatRoomId

        if (chatRoomId == null || chatRoomId.isEmpty()) {
            Log.e(TAG, "ChatRoomId is missing!");
            Toast.makeText(getContext(), "ChatRoomId가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("isCertified", true);
        updateData.put("status", "certified");
        updateData.put("certificationImageUrl", imageUrl);
        updateData.put("certificationDescription", description);

        db.collection("Goals").document(chatRoomId).collection("goals").document(goalId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "목표가 인증되었습니다!", Toast.LENGTH_SHORT).show();

                    // 인증 후 현재 Fragment를 닫기
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update goal: " + e.getMessage());
                });
    }

//    private void endorseGoal(String goalId) {
//        String chatRoomId = getArguments().getString("chatRoomId"); // 전달받은 chatRoomId
//
//        if (chatRoomId == null || chatRoomId.isEmpty()) {
//            Log.e("logchk", "ChatRoomId is missing!");
//            Toast.makeText(getContext(), "ChatRoomId가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Map<String, Object> updateData = new HashMap<>();
//        updateData.put("endorsedByOthers", true); // 기존 인정 필드
//        updateData.put("status", "approved"); // 상태를 'approved'로 설정
//
//        // Firestore 경로
//        DocumentReference goalRef = db.collection("Goals")
//                .document(chatRoomId)
//                .collection("goals")
//                .document(goalId);
//
//        // `goalLikes` 필드 증가
//        goalRef.update(updateData) // 기존 필드 업데이트
//                .addOnSuccessListener(aVoid -> {
//                    // goalLikes 증가
//                    goalRef.update("goalLikes", FieldValue.increment(1))
//                            .addOnSuccessListener(aVoid1 -> {
//                                Toast.makeText(getContext(), "목표가 인정되었습니다!", Toast.LENGTH_SHORT).show();
//
//                                // 창 닫기
//                                if (getActivity() != null) {
//                                    getActivity().getSupportFragmentManager().popBackStack();
//                                }
//                            })
//                            .addOnFailureListener(e -> Log.e("logchk", "Error incrementing goalLikes: " + e.getMessage()));
//                })
//                .addOnFailureListener(e -> Log.e("logchk", "Error endorsing goal: " + e.getMessage()));
//    }

    private void endorseGoal(String goalId) {
        String chatRoomId = getArguments().getString("chatRoomId"); // 전달받은 chatRoomId
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 사용자 ID

        if (chatRoomId == null || chatRoomId.isEmpty()) {
            Log.e("logchk", "ChatRoomId is missing!");
            Toast.makeText(getContext(), "ChatRoomId가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference goalRef = db.collection("Goals")
                .document(chatRoomId)
                .collection("goals")
                .document(goalId);

        // Firestore에 목표 인정 정보 업데이트
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("endorsedByOthers", true);
        updateData.put("status", "approved");

        goalRef.update(updateData) // 상태 업데이트
                .addOnSuccessListener(aVoid -> {
                    // goalLikes 필드 증가
                    goalRef.update("goalLikes", FieldValue.increment(1))
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getContext(), "목표가 인정되었습니다!", Toast.LENGTH_SHORT).show();

                                // 알림 정보 Firestore에 저장
                                goalRef.get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                String goalTitle = documentSnapshot.getString("title");
                                                String goalOwnerId = documentSnapshot.getString("userId"); // 목표 작성자 ID

                                                // 알림 정보 Firestore에 저장
                                                saveNotification(goalOwnerId, goalTitle, chatRoomId, goalId, currentUserId);
                                            }
                                        });

                                // 창 닫기
                                if (getActivity() != null) {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            })
                            .addOnFailureListener(e -> Log.e("logchk", "Error incrementing goalLikes: " + e.getMessage()));
                })
                .addOnFailureListener(e -> Log.e("logchk", "Error endorsing goal: " + e.getMessage()));
    }

    private void saveNotification(String recipientId, String goalTitle, String chatRoomId, String goalId, String senderId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", recipientId); // 알림을 받을 사용자 ID (목표 작성자)
        notificationData.put("title", "목표가 인정받았습니다!");
        notificationData.put("message", "당신의 목표 \"" + goalTitle + "\"이 인정되었습니다!");
        notificationData.put("chatRoomId", chatRoomId);
        notificationData.put("goalId", goalId);
        notificationData.put("senderId", senderId); // 알림을 보낸 사용자 ID
        notificationData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> Log.d("logchk", "Notification saved successfully!"))
                .addOnFailureListener(e -> Log.e("logchk", "Failed to save notification: " + e.getMessage()));
    }




}