//
//package com.example.safedut;
//
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Base64;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Button;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import android.app.AlertDialog;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Base64;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.database.*;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class AdminReportsActivity extends AppCompatActivity {
//
//    private RecyclerView recyclerView;
//    private ReportAdminAdapter adapter;
//    private List<IncidentReport> allReports = new ArrayList<>();
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin_reports);
//
//        recyclerView = findViewById(R.id.adminReportsRecycler);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        loadReports();
//    }
//
//    private void loadReports() {
//        FirebaseDatabase.getInstance().getReference("incident_reports")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        allReports.clear();
//
//                        List<DataSnapshot> snapshots = new ArrayList<>();
//                        for (DataSnapshot snap : snapshot.getChildren()) {
//                            snapshots.add(snap);
//                        }
//
//                        if (snapshots.isEmpty()) {
//                            adapter = new ReportAdminAdapter(allReports);
//                            recyclerView.setAdapter(adapter);
//                            return;
//                        }
//
//                        final int[] fetchedCount = {0};
//
//                        for (DataSnapshot snap : snapshots) {
//                            IncidentReport report = snap.getValue(IncidentReport.class);
//                            if (report == null) continue;
//
//                            report.reportId = snap.getKey();
//
//                            if (report.submittedBy != null && !report.anonymous) {
//                                FirebaseDatabase.getInstance().getReference("users")
//                                        .child(report.submittedBy).child("name")
//                                        .addListenerForSingleValueEvent(new ValueEventListener() {
//                                            @Override
//                                            public void onDataChange(@NonNull DataSnapshot nameSnap) {
//                                                if (nameSnap.exists()) {
//                                                    report.displayName = nameSnap.getValue(String.class);
//                                                }
//                                                allReports.add(report);
//                                                fetchedCount[0]++;
//                                                if (fetchedCount[0] == snapshots.size()) {
//                                                    adapter = new ReportAdminAdapter(allReports);
//                                                    recyclerView.setAdapter(adapter);
//                                                }
//                                            }
//
//                                            @Override
//                                            public void onCancelled(@NonNull DatabaseError error) {
//                                                fetchedCount[0]++;
//                                                if (fetchedCount[0] == snapshots.size()) {
//                                                    adapter = new ReportAdminAdapter(allReports);
//                                                    recyclerView.setAdapter(adapter);
//                                                }
//                                            }
//                                        });
//                            } else {
//                                allReports.add(report);
//                                fetchedCount[0]++;
//                                if (fetchedCount[0] == snapshots.size()) {
//                                    adapter = new ReportAdminAdapter(allReports);
//                                    recyclerView.setAdapter(adapter);
//                                }
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {}
//                });
//    }
//
//
//
//    class ReportAdminAdapter extends RecyclerView.Adapter<ReportAdminAdapter.ViewHolder> {
//        private final List<IncidentReport> reports;
//
//        public ReportAdminAdapter(List<IncidentReport> reports) {
//            this.reports = reports;
//        }
//
//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_report, parent, false);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//            IncidentReport report = reports.get(position);
//            holder.type.setText("Type: " + report.reportType);
//            holder.desc.setText("Description: " + report.description);
//            holder.location.setText(
//                    "Location: " +
//                            (report.location != null && !report.location.isEmpty()
//                                    ? report.location
//                                    : "N/A")
//            );
//            holder.user.setText("Submitted By: " + (
//                    report.anonymous ? "Anonymous"
//                            : (report.displayName != null ? report.displayName : report.submittedBy)
//            ));
//            holder.sorted.setText(report.sorted ? "✅ Sorted" : "⏳ Unsorted");
//
//            if (report.mediaBase64 != null && !report.mediaBase64.trim().isEmpty()) {
//                try {
//                    byte[] decodedBytes = Base64.decode(report.mediaBase64, Base64.DEFAULT);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//
//                    if (bitmap != null) {
//                        holder.media.setImageBitmap(bitmap);
//                        holder.media.setVisibility(View.VISIBLE); // Ensure it's visible
//
//                        holder.media.setOnClickListener(v -> {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(AdminReportsActivity.this);
//                            ImageView imageView = new ImageView(AdminReportsActivity.this);
//                            imageView.setImageBitmap(bitmap);
//                            builder.setView(imageView);
//                            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
//                            builder.show();
//                        });
//                    } else {
//                        holder.media.setVisibility(View.GONE);
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    holder.media.setVisibility(View.GONE);
//                }
//            } else {
//                holder.media.setVisibility(View.GONE);
//            }
//
//
//            if (!report.sorted) {
//                holder.sortButton.setVisibility(View.VISIBLE);
//                holder.sortButton.setOnClickListener(v -> {
//                    FirebaseDatabase.getInstance().getReference("incident_reports")
//                            .child(report.reportId).child("sorted").setValue(true);
//                    report.sorted = true;
//                    notifyItemChanged(position);
//                });
//            } else {
//                holder.sortButton.setVisibility(View.GONE);
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return reports.size();
//        }
//
//        class ViewHolder extends RecyclerView.ViewHolder {
//            TextView type, desc, user, sorted, location;
//            ImageView media;
//            Button sortButton;
//
//            ViewHolder(View itemView) {
//                super(itemView);
//                type = itemView.findViewById(R.id.textType);
//                desc = itemView.findViewById(R.id.textDesc);
//                location = itemView.findViewById(R.id.textLocation);
//                user = itemView.findViewById(R.id.textUser);
//                sorted = itemView.findViewById(R.id.textSorted);
//                media = itemView.findViewById(R.id.imageMedia);
//                sortButton = itemView.findViewById(R.id.btnMarkSorted);
//            }
//        }
//    }
//}


package com.example.safedut;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReportAdminAdapter adapter;
    private EditText searchField;

    // all fetched reports
    private final List<IncidentReport> allReports       = new ArrayList<>();
    // filtered + sorted subset we show
    private final List<IncidentReport> displayedReports = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        recyclerView  = findViewById(R.id.adminReportsRecycler);
        searchField   = findViewById(R.id.searchField);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdminAdapter(displayedReports);
        recyclerView.setAdapter(adapter);

        // live‐search as you type:
        searchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){
                applySortAndFilter(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable e){}
        });

        loadReports();
    }

    private void loadReports() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("incident_reports");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                allReports.clear();

                List<DataSnapshot> snaps = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) snaps.add(s);
                if (snaps.isEmpty()) {
                    applySortAndFilter(searchField.getText().toString());
                    return;
                }


                final int[] remaining = { snaps.size() };

                for (DataSnapshot s : snaps) {
                    IncidentReport r = s.getValue(IncidentReport.class);
                    if (r == null) {
                        if (--remaining[0] == 0) applySortAndFilter(searchField.getText().toString());
                        continue;
                    }
                    r.reportId = s.getKey();

                    if (!r.anonymous && r.submittedBy != null) {
                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(r.submittedBy)
                                .child("name")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override public void onDataChange(@NonNull DataSnapshot nameSnap) {
                                        r.displayName = nameSnap.exists()
                                                ? nameSnap.getValue(String.class)
                                                : null;
                                        allReports.add(r);
                                        if (--remaining[0] == 0)
                                            applySortAndFilter(searchField.getText().toString());
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError e) {
                                        allReports.add(r);
                                        if (--remaining[0] == 0)
                                            applySortAndFilter(searchField.getText().toString());
                                    }
                                });
                    } else {
                        // anonymous or no UID
                        allReports.add(r);
                        if (--remaining[0] == 0)
                            applySortAndFilter(searchField.getText().toString());
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }



    private void applySortAndFilter(String query) {
        // 1) sort
        Collections.sort(allReports, new Comparator<IncidentReport>() {
            @Override public int compare(IncidentReport a, IncidentReport b) {
                // unsorted (sorted==false) first:
                if (a.sorted != b.sorted) {
                    return a.sorted ? +1 /* a sorted->after */ : -1 /* a unsorted->before */;
                }
                // same status: most recent first:
                return Long.compare(b.timestamp, a.timestamp);
            }
        });

        // 2) filter by description:
        displayedReports.clear();
        for (IncidentReport r : allReports) {
            if (r.description.toLowerCase()
                    .contains(query.toLowerCase())) {
                displayedReports.add(r);
            }
        }

        // 3) refresh UI
        adapter.notifyDataSetChanged();
        // scroll to top to show newest unsorted:
        if (!displayedReports.isEmpty())
            recyclerView.scrollToPosition(0);
    }




    class ReportAdminAdapter extends RecyclerView.Adapter<ReportAdminAdapter.ViewHolder> {
        private final List<IncidentReport> reports;
        TextView time;

        public ReportAdminAdapter(List<IncidentReport> reports) {
            this.reports = reports;
        }
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_report, parent, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder,int pos){
            IncidentReport r = reports.get(pos);
            holder.type.setText("Type: "        + r.reportType);
            holder.desc.setText("Description: "+ r.description);
            holder.location.setText("Location:  "+
                    (r.location!=null && !r.location.isEmpty() ? r.location : "N/A")
            );
            // ← new timestamp line
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.time.setText(fmt.format(new Date(r.timestamp)));
            holder.user.setText(
                    "Submitted By: " +
                            (r.anonymous
                                    ? "Anonymous"
                                    : (r.displayName != null
                                    ? r.displayName
                                    : r.submittedBy))
            );
            holder.sorted.setText(r.sorted ? "✅ Sorted" : "⏳ Unsorted");

            // media…
            if (r.mediaBase64!=null && !r.mediaBase64.trim().isEmpty()) {
                try {
                    byte[] data = Base64.decode(r.mediaBase64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(data,0,data.length);
                    holder.media.setImageBitmap(bmp);
                    holder.media.setVisibility(View.VISIBLE);
                    holder.media.setOnClickListener(v->{
                        new AlertDialog.Builder(AdminReportsActivity.this)
                                .setView(new ImageView(AdminReportsActivity.this) {{
                                    setImageBitmap(bmp);
                                }})
                                .setPositiveButton("Close",null)
                                .show();
                    });
                } catch(Exception e){
                    holder.media.setVisibility(View.GONE);
                }
            } else {
                holder.media.setVisibility(View.GONE);
            }

            // mark‐sorted button:
            holder.sortButton.setVisibility(r.sorted? View.GONE: View.VISIBLE);
            holder.sortButton.setOnClickListener(v->{
                FirebaseDatabase.getInstance()
                        .getReference("incident_reports")
                        .child(r.reportId)
                        .child("sorted")
                        .setValue(true);
                r.sorted = true;
                notifyItemChanged(pos);
            });
        }
        @Override public int getItemCount(){ return reports.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView type, desc, location, user, sorted, time;
            ImageView media;
            Button   sortButton;
            ViewHolder(View iv){
                super(iv);
                type       = iv.findViewById(R.id.textType);
                desc       = iv.findViewById(R.id.textDesc);
                location   = iv.findViewById(R.id.textLocation);
                user       = iv.findViewById(R.id.textUser);
                sorted     = iv.findViewById(R.id.textSorted);
                media      = iv.findViewById(R.id.imageMedia);
                sortButton = iv.findViewById(R.id.btnMarkSorted);
                time = itemView.findViewById(R.id.textTime);

            }
        }
    }
}
