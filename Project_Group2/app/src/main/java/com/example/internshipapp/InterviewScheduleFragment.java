package com.example.internshipapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class InterviewScheduleFragment extends Fragment {
    private TextView tvRoleNotice, tvSelectedDateTime;
    private Button btnPickDateTime, btnConfirmSchedule, btnAccept, btnDecline;
    private Calendar selectedCalendar = Calendar.getInstance();
    private String internshipId;
    private String userId;
    private String role;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interview_schedule, container, false);

        tvRoleNotice = view.findViewById(R.id.tvRoleNotice);
        tvSelectedDateTime = view.findViewById(R.id.tvSelectedDateTime);
        btnPickDateTime = view.findViewById(R.id.btnPickDateTime);
        btnConfirmSchedule = view.findViewById(R.id.btnConfirmSchedule);
        btnAccept = view.findViewById(R.id.btnAcceptSchedule);
        btnDecline = view.findViewById(R.id.btnDeclineSchedule);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        internshipId = getArguments() != null ? getArguments().getString("internshipId") : "";

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    role = doc.getString("role");
                    if ("recruiter".equals(role)) {
                        setupRecruiterUI();
                    } else {
                        setupStudentUI();
                    }
                });

        return view;
    }

    private void setupRecruiterUI() {
        tvRoleNotice.setText("You are proposing an interview schedule");
        btnPickDateTime.setVisibility(View.VISIBLE);
        btnConfirmSchedule.setVisibility(View.VISIBLE);
        btnAccept.setVisibility(View.GONE);
        btnDecline.setVisibility(View.GONE);

        btnPickDateTime.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                selectedCalendar.set(year, month, day);
                TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (view2, hour, min) -> {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
                    selectedCalendar.set(Calendar.MINUTE, min);
                    updateLabel();
                }, 12, 0, true);
                timePicker.show();
            }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        btnConfirmSchedule.setOnClickListener(v -> {
            String studentId = getArguments().getString("studentId");
            if (studentId == null) {
                Toast.makeText(getContext(), "No student specified", Toast.LENGTH_SHORT).show();
                return;
            }

            String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(selectedCalendar.getTime());

            Map<String, Object> update = new HashMap<>();
            update.put("interviewTime", formattedTime);
            update.put("status", "Pending");

            String docId = studentId + "_" + internshipId;

            FirebaseFirestore.getInstance().collection("applications")
                    .document(docId)
                    .set(update, SetOptions.merge())
                    .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Schedule set for student", Toast.LENGTH_SHORT).show());
        });
    }


    private void setupStudentUI() {
        tvRoleNotice.setText("You are reviewing an interview proposal");
        btnPickDateTime.setVisibility(View.GONE);
        btnConfirmSchedule.setVisibility(View.GONE);

        String docId = userId + "_" + internshipId;

        FirebaseFirestore.getInstance().collection("applications")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String time = doc.getString("interviewTime");
                        String status = doc.getString("status");

                        if (time != null) {
                            tvSelectedDateTime.setText("Proposed Time: " + time);
                        } else {
                            tvSelectedDateTime.setText("No interview time proposed.");
                        }

                        if (status != null && (status.equals("Accepted") || status.equals("Declined"))) {
                            btnAccept.setVisibility(View.GONE);
                            btnDecline.setVisibility(View.GONE);
                            tvSelectedDateTime.setText("Interview " + status);
                        } else {
                            btnAccept.setVisibility(View.VISIBLE);
                            btnDecline.setVisibility(View.VISIBLE);
                        }
                    }
                });

        btnAccept.setOnClickListener(v -> updateStatus("Accepted"));
        btnDecline.setOnClickListener(v -> updateStatus("Declined"));
    }


    private void updateStatus(String status) {
        String docId = userId + "_" + internshipId;

        FirebaseFirestore.getInstance().collection("applications")
                .document(docId)
                .update("status", status)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Interview " + status, Toast.LENGTH_SHORT).show();

                    btnAccept.setVisibility(View.GONE);
                    btnDecline.setVisibility(View.GONE);

                    tvSelectedDateTime.setText("Interview " + status);
                });
    }


    private void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d yyyy - HH:mm", Locale.getDefault());
        tvSelectedDateTime.setText("Selected: " + sdf.format(selectedCalendar.getTime()));
    }
}
