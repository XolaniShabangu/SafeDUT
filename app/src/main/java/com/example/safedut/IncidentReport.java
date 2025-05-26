//package com.example.safedut;
//
//public class IncidentReport {
//    public String reportType;
//    public String description;
//    public String mediaBase64;
//    public String submittedBy;
//    public boolean anonymous;
//    public boolean sorted;
//    public long timestamp;
//    public String location;
//
//
//    public String displayName; // This will be filled after lookup
//     public String reportId; // 🔑 Firebase key for updates
//
//    public IncidentReport() {}
//
//    public IncidentReport(String reportType, String description, String mediaBase64, String submittedBy, boolean anonymous, boolean sorted, long timestamp, String location) {
//        this.reportType = reportType;
//        this.description = description;
//        this.mediaBase64 = mediaBase64;
//        this.submittedBy = submittedBy;
//        this.anonymous = anonymous;
//        this.sorted = sorted;
//        this.timestamp = timestamp;
//        this.location = location;
//    }
//}

package com.example.safedut;

public class IncidentReport {

    public String reportType;
    public String description;
    public String mediaBase64;
    public String location;
    public String submittedBy;
    public boolean anonymous;
    public boolean sorted;
    public long timestamp;

    public String displayName;
    public String reportId;

    public IncidentReport() {

    }

    public IncidentReport(String reportType, String description, String mediaBase64, String location, String submittedBy, boolean anonymous, boolean sorted, long timestamp) {
        this.reportType = reportType;
        this.description = description;
        this.mediaBase64 = mediaBase64;
        this.location = location;
        this.submittedBy = submittedBy;
        this.anonymous = anonymous;
        this.sorted = sorted;
        this.timestamp = timestamp;
    }

    // Getter methods
    public String getReportType() { return reportType; }
    public String getDescription() { return description; }
    public String getMediaBase64() { return mediaBase64; }
    public String getLocation() { return location; }
    public String getSubmittedBy() { return submittedBy; }
    public boolean isAnonymous() { return anonymous; }
    public boolean isSorted() { return sorted; }
    public long getTimestamp() { return timestamp; }
    public String getDisplayName() { return displayName; }
    public String getReportId() { return reportId; }
}
