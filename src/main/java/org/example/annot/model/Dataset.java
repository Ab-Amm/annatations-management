package org.example.annot.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"classesPossible", "coupleText", "tasks"}) // Add this
@EqualsAndHashCode(exclude = {"classesPossible", "coupleText", "tasks"})
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private boolean annotated = false;

    @OneToMany(mappedBy = "dataset", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ClassePossible> classesPossible;

    @OneToMany(mappedBy = "dataset", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<CoupleText> coupleText;

    @OneToMany(mappedBy = "dataset", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<Task> tasks = new HashSet<>();



}
