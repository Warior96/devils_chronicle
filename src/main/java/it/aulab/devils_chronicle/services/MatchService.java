package it.aulab.devils_chronicle.services;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.aulab.devils_chronicle.models.Match;
import it.aulab.devils_chronicle.repositories.MatchRepository;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${football.api.key:}")
    private String apiKey;

    @Value("${football.api.url:https://api.football-data.org/v4/}")
    private String apiUrl;

    // AC Milan team ID in Football-Data.org
    private static final int AC_MILAN_ID = 98;

    /**
     * Caricamento iniziale dei dati all'avvio dell'applicazione
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadInitialData() {
        System.out.println("Caricamento iniziale dati partite...");
        updateMatchesFromAPI();
    }

    /**
     * Metodo per aggiornare il risultato di una partita
     */
    public Match updateMatchResult(Long matchId, String score) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match != null) {
            match.setScore(score);
            match.setIsPlayed(true);
            return matchRepository.save(match);
        }
        return null;
    }

    /**
     * Recupera l'ultima partita giocata
     */
    public Match getLastMatch() {
        return matchRepository.findTopByIsPlayedTrueOrderByDateDesc();
    }

    /**
     * Recupera la prossima partita
     */
    public Match getNextMatch() {
        return matchRepository.findTopByIsPlayedFalseAndDateAfterOrderByDateAsc(LocalDateTime.now());
    }

    /**
     * Recupera tutte le partite
     */
    public List<Match> getAllMatches() {
        return matchRepository.findAllByOrderByDateDesc();
    }

    /**
     * Recuperare dati da API esterna e aggiornare il database
     */
    @Scheduled(cron = "0 0 */6 * * *") // Ogni 6 ore
    public void updateMatchesFromAPI() {

        System.out.println("Tentativo di aggiornamento partite dall'API...");

        if (apiKey == null || apiKey.isEmpty() || "YOUR_API_KEY_HERE".equals(apiKey)) {
            System.out.println("API Key non configurata, salto l'aggiornamento");
            // Se non c'è API key, crea dati di esempio
            createSampleMatches();
            return;
        }

        try {
            // Chiamata API a Football-Data.org
            String url = apiUrl + "teams/" + AC_MILAN_ID + "/matches?status=SCHEDULED&status=FINISHED&limit=10";

            // Headers per l'autenticazione
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Auth-Token", apiKey);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class);

            // Parsing JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode matches = root.path("matches");

            int processedMatches = 0;

            for (JsonNode matchNode : matches) {
                try {
                    Long externalId = matchNode.path("id").asLong();
                    String homeTeam = matchNode.path("homeTeam").path("name").asText();
                    String awayTeam = matchNode.path("awayTeam").path("name").asText();
                    String competition = matchNode.path("competition").path("name").asText();
                    String utcDate = matchNode.path("utcDate").asText();
                    String status = matchNode.path("status").asText();

                    LocalDateTime date = OffsetDateTime.parse(utcDate).toLocalDateTime();

                    String score = null;
                    Boolean isPlayed = false;
                    if ("FINISHED".equals(status)) {
                        JsonNode fullTime = matchNode.path("score").path("fullTime");
                        int homeGoals = fullTime.path("home").asInt(0);
                        int awayGoals = fullTime.path("away").asInt(0);
                        score = homeGoals + "-" + awayGoals;
                        isPlayed = true;
                    }

                    // Verifica se il match esiste già
                    Match match = matchRepository.findByExternalId(externalId)
                            .orElse(Match.builder().externalId(externalId).build());

                    match.setHomeTeam(homeTeam);
                    match.setAwayTeam(awayTeam);
                    match.setCompetition(competition);
                    match.setDate(date);
                    match.setScore(score);
                    match.setIsPlayed(isPlayed);
                    match.setIsMilanHome("AC Milan".equals(homeTeam));

                    matchRepository.save(match);
                    processedMatches++;
                } catch (Exception e) {
                    System.err.println("Errore nel processare una partita: " + e.getMessage());
                }
            }
            System.out.println("Aggiornate " + processedMatches + " partite dall'API");
        } catch (Exception e) {
            System.err.println("Errore nel recupero dati da API: " + e.getMessage());
            // Se l'API fallisce, crea dati di esempio
            createSampleMatches();
        }
    }

    /**
     * Crea partite di esempio se l'API non è disponibile
     */
    private void createSampleMatches() {
        System.out.println("Creazione dati di esempio per le partite...");

        // Controlla se esistono già partite nel database
        if (matchRepository.count() > 0) {
            System.out.println("Partite già presenti nel database, salto la creazione di esempi");
            return;
        }

        try {
            // Ultima partita giocata (esempio)
            Match lastMatch = Match.builder()
                    .externalId(999001L)
                    .homeTeam("AC Milan")
                    .awayTeam("Inter")
                    .score("7-0")
                    .date(LocalDateTime.now().minusDays(7))
                    .competition("Serie A")
                    .isPlayed(true)
                    .isMilanHome(true)
                    .stadium("San Siro")
                    .build();

            // Prossima partita (esempio)
            Match nextMatch = Match.builder()
                    .externalId(999002L)
                    .homeTeam("AC Milan")
                    .awayTeam("Juventus")
                    .date(LocalDateTime.now().plusDays(7))
                    .competition("Serie A")
                    .isPlayed(false)
                    .isMilanHome(true)
                    .stadium("San Siro")
                    .build();

            matchRepository.save(lastMatch);
            matchRepository.save(nextMatch);

            System.out.println("Partite di esempio create con successo");
        } catch (Exception e) {
            System.err.println("Errore nella creazione delle partite di esempio: " + e.getMessage());
        }
    }

}