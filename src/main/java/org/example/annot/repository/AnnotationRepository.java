package org.example.annot.repository;


import org.example.annot.model.Annotation;
import org.example.annot.model.Annotator;
import org.example.annot.model.CoupleText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {

    Optional<Annotation> findByCoupleTextIdAndAnnotatorId(Long coupleTextId, Long annotatorId);


}
