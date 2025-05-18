package org.example.annot.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(exclude = "tasks")
@Entity
@Data
public class Annotator extends User {

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "annotator")
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "annotator")
    private List<Annotation> annotations = new ArrayList<>();



}
