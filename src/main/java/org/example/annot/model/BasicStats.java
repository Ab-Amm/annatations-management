package org.example.annot.model;

public record BasicStats(
        long totalAnnotations,
        long completedTasks,
        String avgTime
) {}
