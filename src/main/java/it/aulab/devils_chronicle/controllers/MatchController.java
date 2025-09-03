package it.aulab.devils_chronicle.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.devils_chronicle.services.MatchService;

@Controller
@RequestMapping("/admin/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    // Vista per gestione partite (solo lettura)
    @GetMapping
    public String matchesIndex(Model model) {
        System.out.println("Accesso alla pagina di gestione partite");
        
        model.addAttribute("title", "Partite AC Milan");
        model.addAttribute("matches", matchService.getAllMatches());
        
        return "admin/matches";
    }

    // Endpoint per aggiornare manualmente le partite dall'API
    @PostMapping("/update")
    public String updateMatches(RedirectAttributes redirectAttributes) {
        System.out.println("Richiesta di aggiornamento manuale delle partite");
        
        try {
            matchService.updateMatchesFromAPI();
            redirectAttributes.addFlashAttribute("successMessage", "Partite aggiornate con successo dall'API");
            System.out.println("Aggiornamento manuale completato con successo");
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento manuale delle partite: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Errore durante l'aggiornamento delle partite: " + e.getMessage());
        }
        
        return "redirect:/admin/matches";
    }
}