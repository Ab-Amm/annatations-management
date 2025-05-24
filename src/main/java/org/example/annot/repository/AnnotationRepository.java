package org.example.annot.repository;


import org.example.annot.model.Annotation;
import org.example.annot.model.Annotator;
import org.example.annot.model.CoupleText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {

    Optional<Annotation> findByCoupleTextId(Long coupleTextId);

    @Query("SELECT FUNCTION('DATE_FORMAT', a.creationDate, '%Y-%m-%d') AS date, COUNT(a) AS count " +
            "FROM Annotation a " +
            "WHERE a.annotator.id = :annotatorId " +
            "GROUP BY FUNCTION('DATE_FORMAT', a.creationDate, '%Y-%m-%d')")
    List<Object[]> findDailyActivity(Long annotatorId);


    @Query("SELECT AVG(a.timeSpentMillis) FROM Annotation a WHERE a.annotator.id = :annotatorId")
    Double findAverageTimeSpentMillisByAnnotator(Long annotatorId);



    @Query("SELECT a FROM Annotation a WHERE a.annotator.id = :annotatorId ORDER BY a.creationDate DESC")
    Collection<Annotation> findTop5ByAnnotatorIdOrderByCreationDateDesc(Long annotatorId);

    Long countByAnnotatorId(Long annotatorId);

    List<Annotation> findByAnnotator(Annotator annotator);


    @Query("SELECT AVG(a.timeSpentMillis) FROM Annotation a")
    Long findAverageTimeSpentMillis();
}
