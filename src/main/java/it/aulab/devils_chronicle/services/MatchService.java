package it.aulab.devils_chronicle.services;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.aulab.devils_chronicle.models.Match;
import it.aulab.devils_chronicle.models.Standing;
import it.aulab.devils_chronicle.repositories.MatchRepository;
import it.aulab.devils_chronicle.repositories.StandingRepository;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private StandingRepository standingRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${football.api.key:}")
    private String apiKey;

    @Value("${football.api.url:https://api.football-data.org/v4/}")
    private String apiUrl;

    // AC Milan team ID in Football-Data.org
    private static final int AC_MILAN_ID = 98;

    // Serie A competition ID in Football-Data.org
    private static final String SERIE_A_ID = "SA";

    /**
     * Caricamento iniziale dei dati all'avvio dell'applicazione
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadInitialData() {
        System.out.println("=== Avvio caricamento iniziale dati partite ===");

        // Delay di 5 secondi per permettere il completo startup dell'applicazione
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted durante delay iniziale");
        }

        updateMatchesFromAPI();
    }

    /**
     * Aggiornamento automatico ogni 6 ore
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void scheduledUpdate() {
        System.out.println("=== Aggiornamento programmato partite ===");
        updateMatchesFromAPI();
        updateStandingsFromAPI();
    }

    /**
     * Metodo per aggiornare le partite dall'API
     */
    public void updateMatchesFromAPI() {
        System.out.println("Inizio aggiornamento partite dall'API Football-Data.org");

        // Verifica configurazione API
        if (!isApiConfigured()) {
            System.out.println("API Key non configurata o non valida: " + apiKey);
            createSampleMatchesIfEmpty();
            return;
        }

        try {
            // Chiamata all'API per le partite recenti e future
            updateMatchesFromFootballApi();
            System.out.println("Aggiornamento partite completato con successo");

            // Verifica se abbiamo abbastanza partite per la stagione corrente
            long currentSeasonMatches = countCurrentSeasonMatches();
            System.out.println("Partite della stagione corrente trovate: " + currentSeasonMatches);

            if (currentSeasonMatches < 5) {
                System.out.println(
                        "Poche partite trovate dall'API per la stagione corrente, integro con dati di esempio");
                createSampleMatchesIfEmpty();
            }

        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento delle partite dall'API: " + e.getMessage());
            e.printStackTrace();
            createSampleMatchesIfEmpty();
        }
    }

    /**
     * Conta le partite della stagione corrente (2025)
     */
    private long countCurrentSeasonMatches() {
        LocalDateTime startSeason = LocalDateTime.of(2025, 8, 1, 0, 0);
        LocalDateTime endSeason = LocalDateTime.of(2026, 7, 31, 23, 59);

        return matchRepository.findAllByOrderByDateDesc().stream()
                .filter(match -> match.getDate().isAfter(startSeason) && match.getDate().isBefore(endSeason))
                .count();
    }

    /**
     * Verifica se l'API è configurata correttamente
     */
    private boolean isApiConfigured() {
        return apiKey != null &&
                !apiKey.isEmpty() &&
                !apiKey.trim().isEmpty() &&
                !"YOUR_API_KEY_HERE".equals(apiKey) &&
                !"YOUR_FOOTBALL_KEY".equals(apiKey) &&
                !"YOUR_FOOTBALL_DATA_API_KEY".equals(apiKey);
    }

    /**
     * Chiamata effettiva all'API Football-Data.org
     */
    private void updateMatchesFromFootballApi() {
        System.out.println("Chiamata API Football-Data.org per team AC Milan (ID: " + AC_MILAN_ID + ")");

        // URL per le partite della stagione 2025-26
        String url = String.format("%steams/%d/matches?" +
                "dateFrom=2025-07-01&" +
                "dateTo=2026-05-31&" +
                "competitions=SA,CL,EL,UCL,CIT,ISC&" +
                "limit=200",
                apiUrl,
                AC_MILAN_ID);
        System.out.println("URL API (stagione 2025-26): " + url);

        // Headers per autenticazione
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        headers.set("User-Agent", "Devils-Chronicle/1.0");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Chiamata API
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Risposta API ricevuta con successo. Status: " + response.getStatusCode());
                System.out.println("Primi 200 caratteri della risposta: "
                        + response.getBody().substring(0, Math.min(200, response.getBody().length())));
                parseAndSaveMatches(response.getBody());
            } else {
                System.err.println("Errore nella risposta API. Status: " + response.getStatusCode());
                throw new RuntimeException("API Response error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("Errore nella chiamata API: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("API call failed", e);
        }
    }

    /**
     * Parsing e salvataggio delle partite dalla risposta JSON
     */
    private void parseAndSaveMatches(String jsonResponse) {
        System.out.println("Inizio parsing risposta JSON");

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode matches = root.path("matches");

            if (matches.isMissingNode() || !matches.isArray()) {
                System.out.println("Nodo 'matches' non trovato o non è un array nella risposta");
                System.out.println("Struttura JSON ricevuta: " + root.fieldNames());
                return;
            }

            int processedMatches = 0;
            int errorCount = 0;

            System.out.println("Trovate " + matches.size() + " partite da processare");

            for (JsonNode matchNode : matches) {
                try {
                    processAndSaveMatch(matchNode);
                    processedMatches++;
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("Errore nel processare una partita: " + e.getMessage());
                }
            }

            System.out.println(
                    "Processamento completato. Partite elaborate: " + processedMatches + ", Errori: " + errorCount);

        } catch (Exception e) {
            System.err.println("Errore nel parsing JSON: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    /**
     * Processa e salva una singola partita
     */
    private void processAndSaveMatch(JsonNode matchNode) {
        Long externalId = matchNode.path("id").asLong();
        String homeTeam = matchNode.path("homeTeam").path("name").asText();
        String awayTeam = matchNode.path("awayTeam").path("name").asText();
        String competition = matchNode.path("competition").path("name").asText();
        String utcDate = matchNode.path("utcDate").asText();
        String status = matchNode.path("status").asText();
        String venue = matchNode.path("venue").asText();

        // Debug info per ogni partita
        System.out.println("Processing match: " + homeTeam + " vs " + awayTeam + " - " + utcDate + " (" + status + ")");

        // Parsing data
        LocalDateTime matchDate;
        try {
            matchDate = OffsetDateTime.parse(utcDate).toLocalDateTime();
        } catch (DateTimeParseException e) {
            System.err.println("Errore nel parsing della data: " + utcDate);
            return;
        }

        // Parsing risultato
        String score = null;
        Boolean isPlayed = false;

        if ("FINISHED".equals(status)) {
            JsonNode scoreNode = matchNode.path("score").path("fullTime");
            if (!scoreNode.isMissingNode()) {
                int homeGoals = scoreNode.path("home").asInt(-1);
                int awayGoals = scoreNode.path("away").asInt(-1);

                if (homeGoals >= 0 && awayGoals >= 0) {
                    score = homeGoals + "-" + awayGoals;
                    isPlayed = true;
                }
            }
        }

        // Verifica se Milan gioca in casa
        Boolean isMilanHome = "AC Milan".equalsIgnoreCase(homeTeam) ||
                homeTeam.toLowerCase().contains("milan") ||
                "Milan".equalsIgnoreCase(homeTeam);

        // Cerca match esistente o crea nuovo
        Match match = matchRepository.findByExternalId(externalId)
                .orElse(Match.builder()
                        .externalId(externalId)
                        .build());

        // Aggiorna dati match
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setCompetition(competition);
        match.setDate(matchDate);
        match.setScore(score);
        match.setIsPlayed(isPlayed);
        match.setIsMilanHome(isMilanHome);
        match.setStadium(venue.isEmpty() ? null : venue);

        // Salva nel database
        matchRepository.save(match);

        System.out.println(
                "Match salvato: " + homeTeam + " vs " + awayTeam + " - " + matchDate + " (Played: " + isPlayed + ")");
    }

    /**
     * Determina il risultato della partita dal punto di vista del Milan
     * 
     * @param match La partita da analizzare
     * @return "WIN", "DRAW", "LOSS", o null se non giocata
     */
    public String getMilanResult(Match match) {
        if (!Boolean.TRUE.equals(match.getIsPlayed()) || match.getScore() == null) {
            return null;
        }

        try {
            String[] scoreParts = match.getScore().split("-");
            if (scoreParts.length != 2) {
                return null;
            }

            int homeGoals = Integer.parseInt(scoreParts[0].trim());
            int awayGoals = Integer.parseInt(scoreParts[1].trim());

            boolean isMilanHome = Boolean.TRUE.equals(match.getIsMilanHome());

            if (isMilanHome) {
                // Milan gioca in casa
                if (homeGoals > awayGoals)
                    return "WIN";
                else if (homeGoals < awayGoals)
                    return "LOSS";
                else
                    return "DRAW";
            } else {
                // Milan gioca in trasferta
                if (awayGoals > homeGoals)
                    return "WIN";
                else if (awayGoals < homeGoals)
                    return "LOSS";
                else
                    return "DRAW";
            }

        } catch (NumberFormatException e) {
            System.err.println("Errore nel parsing del risultato: " + match.getScore());
            return null;
        }
    }

    /**
     * Ottiene la classe CSS per il badge del risultato dal punto di vista del Milan
     */
    public String getResultBadgeClass(Match match) {
        String result = getMilanResult(match);
        if (result == null) {
            return "bg-success"; // Default per partite non ancora giocate
        }

        switch (result) {
            case "WIN":
                return "bg-success"; // 🟢 Verde per le vittorie
            case "DRAW":
                return "bg-warning"; // 🟡 Giallo per i pareggi
            case "LOSS":
                return "bg-danger"; // 🔴 Rosso per le sconfitte
            default:
                return "bg-success";
        }
    }

    /**
     * Crea partite di esempio realistiche per la stagione 2025/26
     */
    private void createSampleMatchesIfEmpty() {
        long existingMatches = matchRepository.count();
        System.out.println("Partite esistenti nel database: " + existingMatches);

        if (existingMatches > 0) {
            System.out.println("Database già popolato, skip creazione partite di esempio");
            return;
        }

        System.out.println("Creazione partite di esempio per la stagione 2025/26...");

        try {
            // Partite già giocate nella stagione 2025/26 (agosto 2025)
            Match match1 = Match.builder()
                    .externalId(999001L)
                    .homeTeam("AC Milan")
                    .awayTeam("Torino")
                    .score("3-1")
                    .date(LocalDateTime.of(2025, 8, 18, 20, 45))
                    .competition("Serie A")
                    .isPlayed(true)
                    .isMilanHome(true)
                    .stadium("San Siro")
                    .build();

            Match match2 = Match.builder()
                    .externalId(999002L)
                    .homeTeam("Parma")
                    .awayTeam("AC Milan")
                    .score("1-2")
                    .date(LocalDateTime.of(2025, 8, 25, 18, 30))
                    .competition("Serie A")
                    .isPlayed(true)
                    .isMilanHome(false)
                    .stadium("Stadio Ennio Tardini")
                    .build();

            Match match3 = Match.builder()
                    .externalId(999003L)
                    .homeTeam("AC Milan")
                    .awayTeam("Lazio")
                    .score("2-0")
                    .date(LocalDateTime.of(2025, 9, 1, 20, 45))
                    .competition("Serie A")
                    .isPlayed(true)
                    .isMilanHome(true)
                    .stadium("San Siro")
                    .build();

            // Partite future (settembre 2025 in poi)
            Match futureMatch1 = Match.builder()
                    .externalId(999004L)
                    .homeTeam("Fiorentina")
                    .awayTeam("AC Milan")
                    .date(LocalDateTime.of(2025, 9, 14, 20, 45))
                    .competition("Serie A")
                    .isPlayed(false)
                    .isMilanHome(false)
                    .stadium("Stadio Artemio Franchi")
                    .build();

            Match futureMatch2 = Match.builder()
                    .externalId(999005L)
                    .homeTeam("AC Milan")
                    .awayTeam("Bologna")
                    .date(LocalDateTime.of(2025, 9, 21, 15, 0))
                    .competition("Serie A")
                    .isPlayed(false)
                    .isMilanHome(true)
                    .stadium("San Siro")
                    .build();

            // Coppa Italia
            Match coppiaItalia = Match.builder()
                    .externalId(999006L)
                    .homeTeam("AC Milan")
                    .awayTeam("Sassuolo")
                    .date(LocalDateTime.of(2025, 9, 25, 21, 0))
                    .competition("Coppa Italia")
                    .isPlayed(false)
                    .isMilanHome(true)
                    .stadium("San Siro")
                    .build();

            // Derby di Milano
            Match derby = Match.builder()
                    .externalId(999007L)
                    .homeTeam("Inter")
                    .awayTeam("AC Milan")
                    .date(LocalDateTime.of(2025, 9, 28, 20, 45))
                    .competition("Serie A")
                    .isPlayed(false)
                    .isMilanHome(false)
                    .stadium("San Siro")
                    .build();

            // Champions League
            Match championsLeague = Match.builder()
                    .externalId(999008L)
                    .homeTeam("AC Milan")
                    .awayTeam("Real Madrid")
                    .date(LocalDateTime.of(2025, 10, 2, 21, 0))
                    .competition("UEFA Champions League")
                    .isPlayed(false)
                    .isMilanHome(true)
                    .stadium("San Siro")
                    .build();

            // Supercoppa Italiana (dicembre 2025)
            Match supercoppa = Match.builder()
                    .externalId(999009L)
                    .homeTeam("AC Milan")
                    .awayTeam("Juventus")
                    .date(LocalDateTime.of(2025, 12, 15, 20, 0))
                    .competition("Supercoppa Italiana")
                    .isPlayed(false)
                    .isMilanHome(true)
                    .stadium("Al-Awwal Park, Riyadh")
                    .build();

            matchRepository.save(match1);
            matchRepository.save(match2);
            matchRepository.save(match3);
            matchRepository.save(futureMatch1);
            matchRepository.save(futureMatch2);
            matchRepository.save(coppiaItalia);
            matchRepository.save(derby);
            matchRepository.save(championsLeague);
            matchRepository.save(supercoppa);

            System.out.println("Partite di esempio create con successo (9 partite della stagione 2025/26)");

        } catch (Exception e) {
            System.err.println("Errore nella creazione delle partite di esempio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === METODI DI UTILITÀ ===

    /**
     * Aggiorna manualmente risultato di una partita
     */
    public Match updateMatchResult(Long matchId, String score) {
        return matchRepository.findById(matchId).map(match -> {
            match.setScore(score);
            match.setIsPlayed(true);
            return matchRepository.save(match);
        }).orElse(null);
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
     * Recupera tutte le partite ordinate per data
     */
    public List<Match> getAllMatches() {
        return matchRepository.findAllByOrderByDateAsc();
    }

    /**
     * Recupera partite per competizione
     */
    public List<Match> getMatchesByCompetition(String competition) {
        return matchRepository.findByCompetitionOrderByDateDesc(competition);
    }

    /**
     * Metodo principale per aggiornare la classifica dall'API
     */
    public void updateStandingsFromAPI() {
        System.out.println("Inizio aggiornamento classifica dalla API Football-Data.org");

        if (!isApiConfigured()) {
            System.out.println("API Key non configurata, creo classifica di esempio");
            createSampleStandingsIfEmpty();
            return;
        }

        try {
            updateStandingsFromFootballApi();
            System.out.println("Aggiornamento classifica completato con successo");

        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento della classifica dall'API: " + e.getMessage());
            e.printStackTrace();
            createSampleStandingsIfEmpty();
        }
    }

    /**
     * Chiamata effettiva all'API per la classifica Serie A
     */
    private void updateStandingsFromFootballApi() {
        System.out.println("Chiamata API Football-Data.org per classifica Serie A");

        // URL per la classifica Serie A 2025-26
        String url = String.format("%scompetitions/%s/standings?season=2025", apiUrl, SERIE_A_ID);
        System.out.println("URL API classifica: " + url);

        // Headers per autenticazione
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        headers.set("User-Agent", "Devils-Chronicle/1.0");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Risposta API classifica ricevuta con successo");
                parseAndSaveStandings(response.getBody());
            } else {
                System.err.println("Errore nella risposta API classifica. Status: " + response.getStatusCode());
                throw new RuntimeException("API Standings Response error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("Errore nella chiamata API classifica: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("API standings call failed", e);
        }
    }

    /**
     * Parsing e salvataggio della classifica dalla risposta JSON
     */
    private void parseAndSaveStandings(String jsonResponse) {
        System.out.println("Inizio parsing risposta JSON classifica");

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode standings = root.path("standings");

            if (standings.isMissingNode() || !standings.isArray() || standings.size() == 0) {
                System.out.println("Nodo 'standings' non trovato o vuoto");
                return;
            }

            // Prendiamo la prima classifica (di solito è quella generale)
            JsonNode table = standings.get(0).path("table");

            if (table.isMissingNode() || !table.isArray()) {
                System.out.println("Tabella classifica non trovata");
                return;
            }

            int processedTeams = 0;
            int errorCount = 0;

            System.out.println("Trovate " + table.size() + " squadre da processare");

            for (JsonNode teamNode : table) {
                try {
                    processAndSaveStanding(teamNode);
                    processedTeams++;
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("Errore nel processare una squadra: " + e.getMessage());
                }
            }

            System.out.println("Processamento classifica completato. Squadre elaborate: " + processedTeams +
                    ", Errori: " + errorCount);

        } catch (Exception e) {
            System.err.println("Errore nel parsing JSON classifica: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("JSON standings parsing failed", e);
        }
    }

    /**
     * Processa e salva una singola posizione in classifica
     */
    private void processAndSaveStanding(JsonNode teamNode) {
        Long teamId = teamNode.path("team").path("id").asLong();
        String teamName = teamNode.path("team").path("name").asText();
        String teamCrest = teamNode.path("team").path("crest").asText();

        Integer position = teamNode.path("position").asInt();
        Integer playedGames = teamNode.path("playedGames").asInt();
        Integer won = teamNode.path("won").asInt();
        Integer draw = teamNode.path("draw").asInt();
        Integer lost = teamNode.path("lost").asInt();
        Integer points = teamNode.path("points").asInt();
        Integer goalsFor = teamNode.path("goalsFor").asInt();
        Integer goalsAgainst = teamNode.path("goalsAgainst").asInt();
        Integer goalDifference = teamNode.path("goalDifference").asInt();
        String form = teamNode.path("form").asText();

        System.out.println("Processando: " + position + ". " + teamName + " - " + points + " punti");

        // Cerca squadra esistente o crea nuova
        Standing standing = standingRepository.findByTeamId(teamId)
                .orElse(Standing.builder()
                        .teamId(teamId)
                        .build());

        // Aggiorna dati
        standing.setTeamName(teamName);
        standing.setTeamCrest(teamCrest.isEmpty() ? null : teamCrest);
        standing.setPosition(position);
        standing.setPlayedGames(playedGames);
        standing.setWon(won);
        standing.setDraw(draw);
        standing.setLost(lost);
        standing.setPoints(points);
        standing.setGoalsFor(goalsFor);
        standing.setGoalsAgainst(goalsAgainst);
        standing.setGoalDifference(goalDifference);
        standing.setForm(form.isEmpty() ? null : form);

        // Salva nel database
        standingRepository.save(standing);
    }

    /**
     * Restituisce la classifica con Milan centrato (2 sopra, Milan, 2 sotto)
     */
    public List<Standing> getStandingAroundMilan() {
        // Optional<Standing> milanOpt = standingRepository.findMilan();
        Optional<Standing> milanOpt = standingRepository.findByTeamName("AC Milan");


        if (milanOpt.isPresent()) {
            Standing milan = milanOpt.get();
            int milanPosition = milan.getPosition();

            // Calcola range: 2 sopra e 2 sotto Milan
            int startPos = Math.max(1, milanPosition - 2);
            int endPos = Math.min(20, milanPosition + 2);

            return standingRepository.findStandingAroundPosition(startPos, endPos);
        }

        // Se Milan non trovato, restituisci top 5
        return standingRepository.findTop5ByOrderByPositionAsc();
    }

    /**
     * Restituisce la classifica completa
     */
    public List<Standing> getFullStanding() {
        return standingRepository.findAllByOrderByPositionAsc();
    }

    /**
     * Restituisce le prime 5 squadre
     */
    public List<Standing> getTopFiveStanding() {
        return standingRepository.findTop5ByOrderByPositionAsc();
    }

    /**
     * Trova la posizione del Milan
     */
    public Optional<Standing> getMilanStanding() {
        return standingRepository.findByTeamName("AC Milan");
    }

    /**
     * Crea classifica di esempio se il database è vuoto
     */
    private void createSampleStandingsIfEmpty() {
        long existingStandings = standingRepository.count();
        System.out.println("Posizioni esistenti in classifica: " + existingStandings);

        if (existingStandings > 0) {
            System.out.println("Classifica già popolata, skip creazione dati di esempio");
            return;
        }

        System.out.println("Creazione classifica di esempio Serie A 2025/26...");

        try {
            Standing[] sampleStandings = {
                    Standing.builder()
                            .teamId(109L).teamName("Juventus").position(1).playedGames(10)
                            .won(8).draw(1).lost(1).points(25).goalsFor(22).goalsAgainst(8)
                            .goalDifference(14).form("WWDWW").build(),

                    Standing.builder()
                            .teamId(113L).teamName("Napoli").position(2).playedGames(10)
                            .won(7).draw(2).lost(1).points(23).goalsFor(20).goalsAgainst(7)
                            .goalDifference(13).form("WDWWL").build(),

                    Standing.builder()
                            .teamId(108L).teamName("Inter").position(3).playedGames(10)
                            .won(6).draw(3).lost(1).points(21).goalsFor(18).goalsAgainst(9)
                            .goalDifference(9).form("DDWWW").build(),

                    Standing.builder()
                            .teamId(98L).teamName("AC Milan").position(4).playedGames(10)
                            .won(6).draw(2).lost(2).points(20).goalsFor(19).goalsAgainst(12)
                            .goalDifference(7).form("LWWDW").build(),

                    Standing.builder()
                            .teamId(99L).teamName("Fiorentina").position(5).playedGames(10)
                            .won(5).draw(4).lost(1).points(19).goalsFor(17).goalsAgainst(11)
                            .goalDifference(6).form("WDDWW").build(),

                    Standing.builder()
                            .teamId(102L).teamName("Atalanta").position(6).playedGames(10)
                            .won(5).draw(3).lost(2).points(18).goalsFor(21).goalsAgainst(14)
                            .goalDifference(7).form("LWDWW").build(),

                    Standing.builder()
                            .teamId(103L).teamName("Bologna").position(7).playedGames(10)
                            .won(5).draw(2).lost(3).points(17).goalsFor(16).goalsAgainst(13)
                            .goalDifference(3).form("WLWDL").build(),

                    Standing.builder()
                            .teamId(110L).teamName("Lazio").position(8).playedGames(10)
                            .won(4).draw(4).lost(2).points(16).goalsFor(14).goalsAgainst(11)
                            .goalDifference(3).form("DDWLW").build(),

                    Standing.builder()
                            .teamId(104L).teamName("AS Roma").position(9).playedGames(10)
                            .won(4).draw(3).lost(3).points(15).goalsFor(13).goalsAgainst(12)
                            .goalDifference(1).form("LWDDW").build(),

                    Standing.builder()
                            .teamId(115L).teamName("Udinese").position(10).playedGames(10)
                            .won(4).draw(3).lost(3).points(15).goalsFor(12).goalsAgainst(13)
                            .goalDifference(-1).form("DLWDL").build()
            };

            for (Standing standing : sampleStandings) {
                standingRepository.save(standing);
            }

            System.out.println("Classifica di esempio Serie A 2025/26 creata con successo (Top 10)");

        } catch (Exception e) {
            System.err.println("Errore nella creazione della classifica di esempio: " + e.getMessage());
            e.printStackTrace();
        }
    }

}