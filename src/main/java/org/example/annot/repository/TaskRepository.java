package org.example.annot.repository;

import org.example.annot.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Optional: Fetch all tasks for a given annotator
    List<Task> findByAnnotatorId(Long annotatorId);

    // Optional: Fetch tasks for a dataset
    List<Task> findByDatasetId(Long datasetId);
}

