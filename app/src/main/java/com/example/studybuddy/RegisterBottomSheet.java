package com.example.studybuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RegisterBottomSheet extends BottomSheetDialogFragment {

    private OnRegisterListener registerListener;

    public interface OnRegisterListener {
        void onRegister(String subjectName, String memo);
    }

    public void setRegisterListener(OnRegisterListener listener) {
        this.registerListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_register, container, false);

        EditText subjectEditText = view.findViewById(R.id.subject_edit_text);
        EditText memoEditText = view.findViewById(R.id.memo_edit_text);
        Button saveButton = view.findViewById(R.id.save_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        saveButton.setOnClickListener(v -> {
            String subjectName = subjectEditText.getText().toString().trim();
            String memo = memoEditText.getText().toString().trim();

            if (registerListener != null) {
                registerListener.onRegister(subjectName, memo);
            }
            dismiss(); // 바텀시트 닫기
        });

        cancelButton.setOnClickListener(v -> dismiss()); // 바텀시트 닫기

        return view;
    }
}

