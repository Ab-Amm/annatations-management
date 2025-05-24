package org.example.annot.model;


import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.time.Duration;
import java.util.Date;

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

    private Date creationDate = new java.util.Date();


    @Column(name = "time_spent_ms", nullable = false)
    private Long timeSpentMillis = 0L;

    public Duration getTimeSpent() {
        return Duration.ofMillis(timeSpentMillis != null ? timeSpentMillis : 0);
    }

    public void setTimeSpent(Duration duration) {
        this.timeSpentMillis = duration != null ? duration.toMillis() : 0L;
    }
}
