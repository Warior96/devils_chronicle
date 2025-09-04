package it.aulab.devils_chronicle.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import it.aulab.devils_chronicle.models.Standing;

public interface StandingRepository extends JpaRepository<Standing, Long> {

    // Trova classifica completa ordinata per posizione
    List<Standing> findAllByOrderByPositionAsc();

    // Trova una squadra per ID team dell'API
    Optional<Standing> findByTeamId(Long teamId);

    // Trova Milan nella classifica
    // @Query("SELECT s FROM Standing s WHERE LOWER(s.teamName) LIKE '%milan%' OR s.teamName = 'AC Milan'")
    // Optional<Standing> findMilan();
    Optional<Standing> findByTeamName(String teamName);

    // Trova prime N squadre
    List<Standing> findTop5ByOrderByPositionAsc();

    // Trova squadre per range di posizioni
    List<Standing> findByPositionBetweenOrderByPositionAsc(Integer startPos, Integer endPos);

    // Trova classifica con Milan centrato
    @Query("SELECT s FROM Standing s WHERE s.position BETWEEN ?1 AND ?2 ORDER BY s.position ASC")
    List<Standing> findStandingAroundPosition(Integer startPos, Integer endPos);
}