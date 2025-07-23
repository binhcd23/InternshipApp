package com.example.internshipapp;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class ApplicationHistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private ApplicationAdapter adapter;
    private List<ApplicationItem> applicationList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application_history, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewApplications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ApplicationAdapter(applicationList, (application) -> {
            // Thay vì xóa, cập nhật status thành 'Withdrawn'
            new android.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận rút đơn")
                .setMessage("Bạn có chắc chắn muốn rút đơn ứng tuyển này không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    String docId = application.getUserId() + "_" + application.getInternshipId();
                    FirebaseFirestore.getInstance().collection("applications").document(docId)
                            .update("status", "Withdrawn")
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Đã rút đơn ứng tuyển", Toast.LENGTH_SHORT).show();
                                loadApplications();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
        }, (application) -> {
            Bundle bundle = new Bundle();
            bundle.putString("internshipId", application.getInternshipId());
            Navigation.findNavController(view).navigate(R.id.action_applicationHistory_to_interviewScheduleFragment, bundle);
        }, (application) -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", application.getUserId());
            bundle.putString("internshipId", application.getInternshipId());
            bundle.putString("status", application.getStatus());
            Navigation.findNavController(view).navigate(R.id.action_applicationHistory_to_applicationDetailFragment, bundle);
        });

        recyclerView.setAdapter(adapter);
        loadApplications();
        return view;
    }

    private void loadApplications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("applications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    applicationList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        ApplicationItem item = doc.toObject(ApplicationItem.class);
                        applicationList.add(item);
                    }
                    adapter.setData(applicationList);
                });
    }
}
