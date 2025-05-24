package org.example.annot.model;

import java.util.List;
import java.util.Map;

public record AnnotatorStats(
        Long totalAnnotations,
        Long totalTasks,
        Long completedTasks,
        Long pendingTasks,
        String averageTimeSpent,
        double completionRate,
        Map<String, Long> dailyActivity,
        List<TaskProgress> tasks,
        List<RecentAnnotation> recentAnnotations
) {
    public record DailyActivity(String key, Long value) {}
    public record TaskProgress(String dataset, double progress) {}
    public record RecentAnnotation(String text, String chosenClass, java.util.Date annotationDate) {}
}
