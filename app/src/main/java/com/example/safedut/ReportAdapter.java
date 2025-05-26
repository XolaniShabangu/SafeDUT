package com.example.safedut;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private List<IncidentReport> reportList;

    public ReportAdapter(List<IncidentReport> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncidentReport report = reportList.get(position);
        holder.textType.setText("Type: " + report.reportType);
        holder.textDesc.setText("Desc: " + report.description);
        holder.textLocation.setText("Location: " + (report.location != null ? report.location : "Not Provided"));
        holder.textUser.setText("Submitted By: " + (report.displayName != null ? report.displayName : "You"));
        holder.textSorted.setText(report.sorted ? "✅ Sorted" : "⏳ Pending");

        // Show image if available
        if (report.mediaBase64 != null && !report.mediaBase64.isEmpty()) {
            byte[] decodedBytes = Base64.decode(report.mediaBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.imageMedia.setImageBitmap(bitmap);
            holder.imageMedia.setVisibility(View.VISIBLE);

            holder.imageMedia.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                ImageView imageView = new ImageView(v.getContext());
                imageView.setImageBitmap(bitmap);
                builder.setView(imageView);
                builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
        } else {
            holder.imageMedia.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textType, textDesc, textUser, textSorted, textLocation;
        ImageView imageMedia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textType = itemView.findViewById(R.id.textType);
            textDesc = itemView.findViewById(R.id.textDesc);
            textUser = itemView.findViewById(R.id.textUser);
            textSorted = itemView.findViewById(R.id.textSorted);
            textLocation = itemView.findViewById(R.id.textLocation);
            imageMedia = itemView.findViewById(R.id.imageMedia);
        }
    }

}
