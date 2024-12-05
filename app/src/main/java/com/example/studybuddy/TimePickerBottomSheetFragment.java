package com.example.studybuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TimePickerBottomSheetFragment extends BottomSheetDialogFragment {

    private TimePicker timePicker;
    private EditText scheduleInput;
    private Button saveButton;

    private OnScheduleSaveListener listener;

    public interface OnScheduleSaveListener {
        void onScheduleSave(String time, String schedule);
    }

    public void setOnScheduleSaveListener(OnScheduleSaveListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_picker_bottom_sheet, container, false);

        timePicker = view.findViewById(R.id.time_picker);
        scheduleInput = view.findViewById(R.id.schedule_input);
        saveButton = view.findViewById(R.id.save_button);

        saveButton.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format("%02d:%02d", hour, minute);
            String schedule = scheduleInput.getText().toString().trim();

            if (listener != null) {
                listener.onScheduleSave(time, schedule);
            }

            dismiss(); // BottomSheet 닫기
        });

        return view;
    }
}
