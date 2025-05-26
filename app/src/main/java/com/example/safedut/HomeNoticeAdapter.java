package com.example.safedut;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeNoticeAdapter extends RecyclerView.Adapter<HomeNoticeAdapter.VH> {
    private final List<Notice> data;
    private final SimpleDateFormat fmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public HomeNoticeAdapter(List<Notice> data) {
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice_home, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH holder, int pos) {
        Notice n = data.get(pos);
        holder.title.setText(n.title);
        holder.campus.setText("📍 " + n.campus);
        holder.desc.setText(n.description.length() > 60
                ? n.description.substring(0, 57) + "…"
                : n.description
        );
        holder.time.setText(fmt.format(new Date(n.timestamp)));

        // **Make the whole card clickable:**
        holder.itemView.setOnClickListener(v -> {
            String msg =
                    "Campus: "    + n.campus + "\n\n" +
                            "Description:\n" + n.description + "\n\n" +
                            "Time: "      + fmt.format(new Date(n.timestamp));

            new AlertDialog.Builder(v.getContext())
                    .setTitle(n.title)
                    .setMessage(msg)
                    .setPositiveButton("Close", null)
                    .show();
        });
    }

    @Override public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, campus, desc, time;
        VH(View itemView) {
            super(itemView);
            title  = itemView.findViewById(R.id.tvNoticeTitle);
            campus = itemView.findViewById(R.id.tvNoticeCampus);
            desc   = itemView.findViewById(R.id.tvNoticeDesc);
            time   = itemView.findViewById(R.id.tvNoticeTime);
        }
    }
}
