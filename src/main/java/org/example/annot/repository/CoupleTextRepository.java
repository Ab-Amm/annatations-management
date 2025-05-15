package org.example.annot.repository;

import org.example.annot.model.CoupleText;
import org.example.annot.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoupleTextRepository extends JpaRepository<CoupleText, Long>{
    List<CoupleText> findByDataset(Dataset dataset);
    @Query("SELECT ct FROM CoupleText ct WHERE ct.dataset = :dataset AND ct NOT IN (SELECT c FROM Task t JOIN t.coupleTexts c)")
    List<CoupleText> findUnassignedTextsByDataset(@Param("dataset") Dataset dataset);


}
