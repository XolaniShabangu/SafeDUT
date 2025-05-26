package com.example.safedut;

public class Notice {
    public String id;
    public String title;
    public String campus;
    public String description;
    public boolean resolved;
    public long timestamp;


    public Notice() {}

    public Notice(String id,
                  String title,
                  String campus,
                  String description,
                  boolean resolved,
                  long timestamp) {
        this.id = id;
        this.title = title;
        this.campus = campus;
        this.description = description;
        this.resolved = resolved;
        this.timestamp = timestamp;
    }
}
