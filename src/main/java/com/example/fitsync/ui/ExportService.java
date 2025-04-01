package com.example.fitsync.ui;

import com.example.fitsync.model.User;

public class ExportService {
    public static void exportSummary(User user) {
        System.out.println(" Exporting daily summary for user: " + user.getName());
        // Placeholder: Add actual file generation here
    }

    public static void exportProgress(User user) {
        System.out.println(" Exporting weekly progress for user: " + user.getName());
        // Placeholder: Add actual file generation here
    }
}
