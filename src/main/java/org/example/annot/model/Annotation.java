package org.example.annot.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"annotator", "coupleText"})
@EqualsAndHashCode(exclude = {"annotator", "coupleText"})
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chosenClass;

    @ManyToOne
    private Annotator annotator;

    @OneToOne
    @JoinColumn(name = "couple_text_id", unique = true)
    private CoupleText coupleText;


}
