package it.aulab.devils_chronicle.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID del match fornito dall'API
    @Column(unique = true)
    private Long externalId;

    @Column(nullable = false)
    private String homeTeam;

    @Column(nullable = false)
    private String awayTeam;

    @Column
    private String score;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column
    private String competition;

    @Builder.Default
    @Column
    private Boolean isPlayed = false;

    @Column
    private String stadium;

    @Column(length = 500)
    private String notes;

    @Column
    private Boolean isMilanHome;

}
