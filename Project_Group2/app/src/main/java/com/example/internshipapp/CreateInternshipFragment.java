package com.example.internshipapp;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class CreateInternshipFragment extends Fragment {

    private EditText edtTitle, edtField, edtDescription, edtRental;
    private TextView edtCompany, tvDatePosted;
    private Spinner spinnerDuration;
    private Button btnSubmitInternship;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String[] durations = {"3 months", "6 months", "1 year"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_internship, container, false);

        // Khởi tạo View
        edtTitle = view.findViewById(R.id.edtTitle);
        edtCompany = view.findViewById(R.id.edtCompany);
        edtField = view.findViewById(R.id.edtField);
        edtDescription = view.findViewById(R.id.edtDescription);
        spinnerDuration = view.findViewById(R.id.spinnerDuration);
        edtRental = view.findViewById(R.id.edtRental);
        tvDatePosted = view.findViewById(R.id.tvDatePosted);
        btnSubmitInternship = view.findViewById(R.id.btnSubmitInternship);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set Spinner values
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, durations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDuration.setAdapter(adapter);

        // Gán ngày hiện tại
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvDatePosted.setText("Date Posted: " + currentDate);

        // Lấy tên công ty và gán viết hoa + in đậm
        String recruiterId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(recruiterId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String company = documentSnapshot.getString("organization");
                        if (company != null) {
                            edtCompany.setText(company.toUpperCase(Locale.getDefault()));
                        }
                    }
                });

        btnSubmitInternship.setOnClickListener(v -> submitInternship());

        return view;
    }

    private void submitInternship() {
        String title = edtTitle.getText().toString().trim();
        String company = edtCompany.getText().toString().trim();
        String field = edtField.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String duration = spinnerDuration.getSelectedItem().toString();
        String rental = edtRental.getText().toString().trim();
        String datePosted = tvDatePosted.getText().toString().replace("Date Posted: ", "").trim();
        String recruiterId = mAuth.getCurrentUser().getUid();

        if (title.isEmpty() || field.isEmpty() || description.isEmpty() || rental.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> internship = new HashMap<>();
        internship.put("title", title);
        internship.put("company", company);
        internship.put("field", field);
        internship.put("description", description);
        internship.put("duration", duration);
        internship.put("rental", rental);
        internship.put("datepost", datePosted);
        internship.put("recruiterId", recruiterId);

        db.collection("internships")
                .add(internship)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Internship posted successfully", Toast.LENGTH_SHORT).show();
                    NavController navController = NavHostFragment.findNavController(CreateInternshipFragment.this);
                    navController.navigate(R.id.action_createInternshipFragment_to_recruiterHomeFragment);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to post internship", Toast.LENGTH_SHORT).show();
                });
    }

}
