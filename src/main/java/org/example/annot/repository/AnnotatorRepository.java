package org.example.annot.repository;


import org.example.annot.model.Annotator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnnotatorRepository extends JpaRepository<Annotator, Long> {
    List<Annotator> findByDeletedFalse();

    boolean existsByUsername(String username);

    @Query("SELECT a FROM Annotator a " +
            "LEFT JOIN FETCH a.tasks t " +
            "LEFT JOIN FETCH t.dataset " +
            "LEFT JOIN FETCH t.coupleTexts " +
            "WHERE a.username = :username")
    Optional<Annotator> findByUsernameWithTasks(@Param("username") String username);

    Optional<Annotator> findByUsername(String name);

}

