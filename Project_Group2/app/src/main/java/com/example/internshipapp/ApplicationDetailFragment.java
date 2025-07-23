package com.example.internshipapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ApplicationDetailFragment extends Fragment {
    private TextView tvInternshipId, tvStatus, tvResume, tvDate, tvFeedback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application_detail, container, false);
        tvInternshipId = view.findViewById(R.id.tvInternshipIdDetail);
        tvStatus = view.findViewById(R.id.tvStatusDetail);
        tvResume = view.findViewById(R.id.tvResumeDetail);
        tvDate = view.findViewById(R.id.tvDateDetail);
        tvFeedback = view.findViewById(R.id.tvFeedbackDetail);

        Bundle args = getArguments();
        if (args != null) {
            String userId = args.getString("userId");
            String internshipId = args.getString("internshipId");
            if (userId != null && internshipId != null) {
                String docId = userId + "_" + internshipId;
                FirebaseFirestore.getInstance().collection("applications")
                        .document(docId)
                        .get()
                        .addOnSuccessListener(this::showApplicationDetail)
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi tải chi tiết đơn", Toast.LENGTH_SHORT).show());
            }
        }
        return view;
    }

    private void showApplicationDetail(DocumentSnapshot doc) {
        if (doc.exists()) {
            tvInternshipId.setText("Internship: " + doc.getString("internshipId"));
            tvStatus.setText("Status: " + doc.getString("status"));
            tvResume.setText("Resume: " + doc.getString("resumeText"));
            String date = doc.contains("date") ? doc.getString("date") : "N/A";
            tvDate.setText("Date: " + date);
            String feedback = doc.contains("feedback") ? doc.getString("feedback") : "Chưa có phản hồi";
            tvFeedback.setText("Feedback: " + feedback);
        }
    }
} 