package it.aulab.devils_chronicle.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.aulab.devils_chronicle.models.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // Trova l'ultima partita giocata
    Match findTopByIsPlayedTrueOrderByDateDesc();

    // Trova la prossima partita da giocare
    Match findTopByIsPlayedFalseAndDateAfterOrderByDateAsc(LocalDateTime date);

    // Trova tutte le partite ordinate per data
    List<Match> findAllByOrderByDateDesc();

    // Trova partite per competizione
    List<Match> findByCompetitionOrderByDateDesc(String competition);

    // Trova partita per ID esterno dell'API
    Optional<Match> findByExternalId(Long externalId);

}
