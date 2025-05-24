package org.example.annot.service;

import jakarta.transaction.Transactional;
import org.example.annot.model.Annotator;
import org.example.annot.model.AnnotatorStats;
import org.example.annot.model.BasicStats;
import org.example.annot.model.Task;
import org.example.annot.repository.AnnotationRepository;
import org.example.annot.repository.AnnotatorRepository;
import org.example.annot.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;


import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnnotatorStatsService {

    private final AnnotationRepository annotationRepository;
    private final TaskRepository taskRepository;
    private final AnnotatorRepository annotatorRepository;

    public AnnotatorStatsService(AnnotationRepository annotationRepository, TaskRepository taskRepository, AnnotatorRepository annotatorRepository) {
        this.annotationRepository = annotationRepository;
        this.taskRepository = taskRepository;
        this.annotatorRepository = annotatorRepository;
    }

    private String formatAverageDuration(Double avgMillis) {
        if (avgMillis == null || avgMillis == 0) {
            return "0m";
        }
        long avg = avgMillis.longValue();
        Duration avgDuration = Duration.ofMillis(avg);
        return String.format("%dm %ds",
                avgDuration.toMinutes(),
                avgDuration.toSecondsPart());
    }

    public Map<String, Long> getDailyActivity(Long annotatorId) {
        List<Object[]> raw = annotationRepository.findDailyActivity(annotatorId);
        return raw.stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (Long) r[1],
                        (v1, v2) -> v1, // In case of duplicate keys, keep the first
                        LinkedHashMap::new // maintain insertion order
                ));
    }

    public AnnotatorStats getAnnotatorStats(Long annotatorId) {
        // Calculate total annotations
        Long totalAnnotations = annotationRepository.countByAnnotatorId(annotatorId);

        // Get task statistics
        List<Task> tasks = taskRepository.findByAnnotatorId(annotatorId);
        Long totalTasks = (long) tasks.size();
        Long completedTasks = tasks.stream().filter(Task::isAllCouplesDone).count();
        Long pendingTasks = totalTasks - completedTasks;

        // Calculate average time
        Double avgMillis = annotationRepository.findAverageTimeSpentMillisByAnnotator(annotatorId);
        String averageTime = formatAverageDuration(avgMillis);

        // Calculate daily activity
        Map<String, Long> dailyActivity = getDailyActivity(annotatorId);

        // Prepare recent annotations
        List<   AnnotatorStats.RecentAnnotation> recentAnnotations = annotationRepository.findTop5ByAnnotatorIdOrderByCreationDateDesc(annotatorId)
                .stream()
                .map(a -> new AnnotatorStats.RecentAnnotation(
                        a.getCoupleText().getText1() + " / " + a.getCoupleText().getText2(),
                        a.getChosenClass(),
                        a.getCreationDate()
                ))
                .toList();

        // Calculate completion rate
        double completionRate = tasks.isEmpty() ? 0 :
                (completedTasks.doubleValue() / totalTasks.doubleValue()) * 100;

        return new AnnotatorStats(
                totalAnnotations,
                totalTasks,
                completedTasks,
                pendingTasks,
                averageTime,
                completionRate,
                dailyActivity,
                mapTaskProgress(tasks),
                recentAnnotations
        );
    }

    private String formatDuration(Duration duration) {
        if (duration == null) return "0m";
        return duration.toMinutes() + "m " + duration.toSecondsPart() + "s";
    }

    private List<AnnotatorStats.TaskProgress> mapTaskProgress(List<Task> tasks) {
        return tasks.stream()
                .map(t -> new AnnotatorStats.TaskProgress(
                        t.getDataset().getName(),
                        (t.getCoupleTexts().isEmpty()) ? 0 :
                                ((double) t.getCouplesDone() / t.getCoupleTexts().size()) * 100
                ))
                .toList();
    }

    public Map<Long, BasicStats> getBasicStatsForAll() {
        List<Annotator> annotators = annotatorRepository.findAll();
        return annotators.stream()
                .collect(Collectors.toMap(
                        Annotator::getId,
                        annotator -> {
                            Long annotatorId = annotator.getId();
                            return new BasicStats(
                                    annotationRepository.countByAnnotatorId(annotatorId),
                                    taskRepository.countCompletedTasksByAnnotatorId(annotatorId),
                                    formatAverageDuration(
                                            annotationRepository.findAverageTimeSpentMillisByAnnotator(annotatorId)
                                    )
                            );
                        }
                ));
    }

    public Long getAverageTimeSpent() {
        return annotationRepository.findAverageTimeSpentMillis();
    }


}
