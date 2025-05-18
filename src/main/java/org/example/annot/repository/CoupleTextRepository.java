package org.example.annot.repository;

import org.example.annot.model.CoupleText;
import org.example.annot.model.Dataset;
import org.example.annot.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CoupleTextRepository extends JpaRepository<CoupleText, Long>{
    List<CoupleText> findByDataset(Dataset dataset);
    @Query("SELECT ct FROM CoupleText ct WHERE ct.dataset = :dataset AND ct NOT IN (SELECT c FROM Task t JOIN t.coupleTexts c)")
    List<CoupleText> findUnassignedTextsByDataset(@Param("dataset") Dataset dataset);


    List<CoupleText> findByTask(Task task);

    List<CoupleText> findByTaskIdOrderById(Long taskId);
}
