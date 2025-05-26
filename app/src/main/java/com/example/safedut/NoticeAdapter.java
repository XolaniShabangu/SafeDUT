package com.example.safedut;

import android.app.AlertDialog;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {
    private final List<Notice> notices;

    public NoticeAdapter(List<Notice> notices) {
        this.notices = notices;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Notice n = notices.get(pos);

        h.tvTitle.setText(n.title);
        h.tvCampus.setText("Campus: " + n.campus);
        h.tvDescription.setText(n.description);

        String ts = DateFormat.format("yyyy-MM-dd HH:mm", new Date(n.timestamp)).toString();
        h.tvTimestamp.setText(ts);


        if (n.resolved) {
            h.btnToggleResolved.setText("Resolved");
            h.btnToggleResolved.setEnabled(false);
            ((CardView)h.itemView).setCardBackgroundColor(Color.parseColor("#E0E0E0"));
        } else {
            h.btnToggleResolved.setText("Mark Resolved");
            h.btnToggleResolved.setEnabled(true);
            ((CardView)h.itemView).setCardBackgroundColor(Color.WHITE);
        }


        h.btnToggleResolved.setOnClickListener(v ->
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Mark as resolved?")
                        .setPositiveButton("Yes", (dlg, which) -> {

                            FirebaseDatabase.getInstance()
                                    .getReference("notices")
                                    .child(n.id)
                                    .child("resolved")
                                    .setValue(true);


                            n.resolved = true;
                            notifyItemChanged(pos);
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }

    @Override public int getItemCount() { return notices.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCampus, tvDescription, tvTimestamp;
        Button btnToggleResolved;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle          = itemView.findViewById(R.id.tvTitle);
            tvCampus         = itemView.findViewById(R.id.tvCampus);
            tvDescription    = itemView.findViewById(R.id.tvDescription);
            tvTimestamp      = itemView.findViewById(R.id.tvTimestamp);
            btnToggleResolved= itemView.findViewById(R.id.btnToggleResolved);
        }
    }
}
