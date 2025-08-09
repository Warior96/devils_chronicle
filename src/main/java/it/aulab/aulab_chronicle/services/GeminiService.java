package it.aulab.aulab_chronicle.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    @Autowired
    private RestTemplate restTemplate; // Bean di Spring per effettuare chiamate HTTP REST

    // URL base del microservizio AI (Flask + Gemini)
    private final String aiMicroserviceUrl = "http://localhost:5006";

    /**
     * Metodo che invia titolo e contenuto dell'articolo al microservizio AI
     * per ottenere un riassunto.
     *
     * @param title   Titolo dell'articolo
     * @param content Contenuto completo dell'articolo
     * @return Riassunto generato dall'AI o messaggio di errore
     */
    public String getSummary(String title, String content) {
        String url = aiMicroserviceUrl + "/summarize";

        // Setto le intestazioni HTTP per indicare che il corpo è JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Corpo della richiesta, mappa chiave-valore con titolo e contenuto
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", title);
        requestBody.put("content", content);

        // Incapsulo headers e corpo in un HttpEntity per la chiamata REST
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            // Chiamo il microservizio con POST e ricevo una risposta mappata in Map
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null) {
                // Se la risposta contiene il campo "response" (riassunto), lo ritorno
                if (response.containsKey("response")) {
                    return (String) response.get("response");
                    // Se invece c'è un errore, lo ritorno come stringa
                } else if (response.containsKey("error")) {
                    return "Errore AI: " + response.get("error");
                }
            }
            // Caso in cui la risposta non ha i campi attesi
            return "Risposta AI non valida";

        } catch (Exception e) {
            e.printStackTrace();
            // In caso di eccezione nella chiamata HTTP ritorno un messaggio d'errore
            return "Errore durante la chiamata al microservizio AI: " + e.getMessage();
        }
    }

    /**
     * Metodo che invia titolo, contenuto e domanda al microservizio AI
     * per ottenere una risposta basata sull'articolo.
     *
     * @param title    Titolo dell'articolo
     * @param content  Contenuto completo dell'articolo
     * @param question Domanda dell'utente da porre all'AI
     * @return Risposta generata dall'AI o messaggio di errore
     */
    public String askQuestion(String title, String content, String question) {
        String url = aiMicroserviceUrl + "/answer";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("title", title);
        requestBody.put("content", content);
        requestBody.put("question", question);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null) {
                if (response.containsKey("response")) {
                    return (String) response.get("response");
                } else if (response.containsKey("error")) {
                    return "Errore AI: " + response.get("error");
                }
            }
            return "Risposta AI non valida";

        } catch (Exception e) {
            e.printStackTrace();
            return "Errore durante la chiamata al microservizio AI: " + e.getMessage();
        }
    }

}
