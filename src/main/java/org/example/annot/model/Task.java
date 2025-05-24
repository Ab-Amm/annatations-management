package org.example.annot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"annotator", "dataset", "coupleTexts"}) // Add this
@EqualsAndHashCode(exclude = {"annotator", "dataset", "coupleTexts"}) // Add this
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate deadline;

    @Column(nullable = false)
    private int couplesDone = 0;

    @Column(nullable = false)
    private boolean allCouplesDone = false;

    @ManyToOne(fetch = FetchType.EAGER)
    private Annotator annotator;

    @ManyToOne
    private Dataset dataset;

    @OneToMany(mappedBy = "task")
    private List<CoupleText> coupleTexts = new ArrayList<>();

}

