package com.example.internshipapp;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;
// Adapter cho RecyclerView hiển thị danh sách các đơn ứng tuyển
public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {
    private List<ApplicationItem> applications;
    private final OnWithdrawClick withdrawClick;
    private final OnScheduleClick scheduleClick;
    private final OnItemClickListener itemClickListener;

    public interface OnWithdrawClick { void onWithdraw(ApplicationItem app); }
    public interface OnScheduleClick { void onSchedule(ApplicationItem app); }
    public interface OnItemClickListener { void onItemClick(ApplicationItem app); }

    public ApplicationAdapter(List<ApplicationItem> apps, OnWithdrawClick wc, OnScheduleClick sc, OnItemClickListener icl) {
        this.applications = apps;
        this.withdrawClick = wc;
        this.scheduleClick = sc;
        this.itemClickListener = icl;
    }

    public void setData(List<ApplicationItem> newData) {
        applications = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout từng item trong danh sách
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationItem item = applications.get(position);
        holder.tvInternshipId.setText("Internship: " + item.getInternshipId());
        holder.tvStatus.setText("Status: " + item.getStatus());

        if ("Withdrawn".equalsIgnoreCase(item.getStatus())) {
            holder.btnWithdraw.setVisibility(View.GONE);
            holder.tvStatus.setTextColor(0xFF888888); // xám
            holder.btnSchedule.setVisibility(View.GONE);
        } else if ("Declined".equalsIgnoreCase(item.getStatus())) {
            holder.btnSchedule.setVisibility(View.GONE);
            holder.btnWithdraw.setVisibility(View.VISIBLE);
            holder.tvStatus.setTextColor(0xFF000000); // đen
            holder.btnWithdraw.setOnClickListener(v -> withdrawClick.onWithdraw(item));
        } else {
            holder.btnWithdraw.setVisibility(View.VISIBLE);
            holder.tvStatus.setTextColor(0xFF000000); // đen
            holder.btnWithdraw.setOnClickListener(v -> withdrawClick.onWithdraw(item));
            holder.btnSchedule.setVisibility(View.VISIBLE);
        }
        holder.btnSchedule.setOnClickListener(v -> scheduleClick.onSchedule(item));
        holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInternshipId, tvStatus;
        Button btnWithdraw, btnSchedule;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInternshipId = itemView.findViewById(R.id.tvInternshipId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnWithdraw = itemView.findViewById(R.id.btnWithdraw);
            btnSchedule = itemView.findViewById(R.id.btnSchedule);
        }
    }
}

