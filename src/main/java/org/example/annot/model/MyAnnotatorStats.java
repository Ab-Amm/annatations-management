package org.example.annot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MyAnnotatorStats(
        Long totalAnnotations,
        Long completedTasks,
        Long activeTasks,
        Integer datasetsWorked,
        String averageTime,
        List<TaskProgress> tasks,
        List<DailyActivity> dailyActivity,
        List<RecentAnnotation> recentAnnotations
) {
    public record TaskProgress(
            String datasetName,
            LocalDate deadline,
            Integer progress,
            Integer completed,
            Integer total
    ) {}

    public record DailyActivity(String key, Long value) {}

    public record RecentAnnotation(
            String text1,
            String text2,
            String chosenClass,
            LocalDateTime date,
            String timeSpent
    ) {}
}
