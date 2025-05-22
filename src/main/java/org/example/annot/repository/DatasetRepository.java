package org.example.annot.repository;

import org.example.annot.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    Dataset findByAssignedFalse();


    @Query("SELECT d FROM Dataset d " +
            "LEFT JOIN FETCH d.coupleText ct " +
            "LEFT JOIN FETCH ct.annotation " +
            "WHERE d.id = :id")
    Optional<Dataset> findByIdWithAnnotations(@Param("id") Long id);
}
