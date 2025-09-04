package it.aulab.devils_chronicle.models;

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
@Table(name = "standings")
public class Standing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long teamId;

    @Column(nullable = false)
    private String teamName;

    @Column
    private String teamCrest;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Integer playedGames;

    @Column(nullable = false)
    private Integer won;

    @Column(nullable = false)
    private Integer draw;

    @Column(nullable = false)
    private Integer lost;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Integer goalsFor;

    @Column(nullable = false)
    private Integer goalsAgainst;

    @Column(nullable = false)
    private Integer goalDifference;

    @Column
    private String form;

    @Column
    private Integer homeWins;

    @Column
    private Integer homeDraw;

    @Column
    private Integer homeLosses;

    @Column
    private Integer awayWins;

    @Column
    private Integer awayDraw;

    @Column
    private Integer awayLosses;

    public Integer getHomeGames() {
        return (homeWins != null ? homeWins : 0) +
                (homeDraw != null ? homeDraw : 0) +
                (homeLosses != null ? homeLosses : 0);
    }

    public Integer getAwayGames() {
        return (awayWins != null ? awayWins : 0) +
                (awayDraw != null ? awayDraw : 0) +
                (awayLosses != null ? awayLosses : 0);
    }

    public Double getPointsPerGame() {
        if (playedGames == null || playedGames == 0)
            return 0.0;
        return (double) points / playedGames;
    }

    public Double getGoalsPerGame() {
        if (playedGames == null || playedGames == 0)
            return 0.0;
        return (double) goalsFor / playedGames;
    }

    public Boolean isMilan() {
        return teamName != null &&
                (teamName.toLowerCase().contains("milan") ||
                        teamName.equalsIgnoreCase("AC Milan"));
    }
}