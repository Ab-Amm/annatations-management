package org.example.annot.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoupleText {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text1;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text2;

    @ManyToOne
    private Dataset dataset;

    public CoupleText(String trim, String trim1, Dataset dataset) {
        this.text1 = trim;
        this.text2 = trim1;
        this.dataset = dataset;
    }

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

}
