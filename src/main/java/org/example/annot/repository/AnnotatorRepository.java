package org.example.annot.repository;


import org.example.annot.model.Annotator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnotatorRepository extends JpaRepository<Annotator, Long> {
    List<Annotator> findByDeletedFalse();
}

