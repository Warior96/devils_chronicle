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
        model.addAttribute("title", "Partite AC Milan");
        model.addAttribute("matches", matchService.getAllMatches());
        return "admin/matches";
    }

    // Endpoint per aggiornare manualmente le partite dall'API
    @PostMapping("/update")
    public String updateMatches(RedirectAttributes redirectAttributes) {
        try {
            matchService.updateMatchesFromAPI();
            redirectAttributes.addFlashAttribute("successMessage", "Partite aggiornate con successo dall'API");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Errore durante l'aggiornamento delle partite: " + e.getMessage());
        }
        return "redirect:/admin/matches";
    }
    
}
