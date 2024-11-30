package com.example.studybuddy;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ScheduleRegisterDialog extends DialogFragment {

    private String selectedDate;
    private ScheduleRegisterCallback callback;

    public ScheduleRegisterDialog(String selectedDate, ScheduleRegisterCallback callback) {
        this.selectedDate = selectedDate;
        this.callback = callback;
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.dialog_register_schedule);

        EditText inputSchedule = dialog.findViewById(R.id.input_schedule);
        TimePicker timePicker = dialog.findViewById(R.id.time_picker);
        Button saveButton = dialog.findViewById(R.id.save_button);

        saveButton.setOnClickListener(v -> {
            String scheduleTitle = inputSchedule.getText().toString().trim();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format("%02d:%02d", hour, minute);

            if (!scheduleTitle.isEmpty()) {
                Schedule schedule = new Schedule(selectedDate, scheduleTitle, time);
                callback.onScheduleRegistered(schedule);
                dismiss();
            }
        });

        return dialog;
    }

    public interface ScheduleRegisterCallback {
        void onScheduleRegistered(Schedule schedule);
    }
}
