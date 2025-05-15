package org.example.annot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.*;

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

}
