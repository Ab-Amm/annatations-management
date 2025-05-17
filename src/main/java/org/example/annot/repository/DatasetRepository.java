package org.example.annot.repository;

import org.example.annot.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    Dataset findByAnnotatedFalse();



}
