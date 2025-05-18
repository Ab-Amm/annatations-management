package org.example.annot.repository;

import org.example.annot.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Optional: Fetch all tasks for a given annotator
    List<Task> findByAnnotatorId(Long annotatorId);

    // Optional: Fetch tasks for a dataset
    List<Task> findByDatasetId(Long datasetId);

    // In TaskRepository
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.coupleTexts WHERE t.id = :id")
    Optional<Task> findByIdWithCouples(@Param("id") Long id);

}

