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

    // Vista per gestione partite
    @GetMapping
    public String matchesIndex(Model model) {
        System.out.println("Access to the game management page");

        model.addAttribute("title", "Partite AC Milan");
        model.addAttribute("matches", matchService.getAllMatches());
        model.addAttribute("fullStanding", matchService.getFullStanding());
        model.addAttribute("standingAroundMilan", matchService.getStandingAroundMilan());
        model.addAttribute("milanStanding", matchService.getMilanStanding().orElse(null));
        model.addAttribute("matchService", matchService);

        return "admin/matches";
    }

    // Endpoint per aggiornare manualmente le partite dall'API
    @PostMapping("/update")
    public String updateMatches(RedirectAttributes redirectAttributes) {
        System.out.println("Request for manual update of matches");

        try {
            matchService.updateMatchesFromAPI();
            redirectAttributes.addFlashAttribute("successMessage", "Games successfully updated by the API");
            System.out.println("Manual update successfully completed");
        } catch (Exception e) {
            System.err.println("Error during manual game update: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error while updating matches: " + e.getMessage());
        }

        return "redirect:/admin/matches";
    }

    // Endpoint per aggiornare manualmente la classifica dall'API
    @PostMapping("/update-standings")
    public String updateStandings(RedirectAttributes redirectAttributes) {
        System.out.println("Request for manual update of the ranking");

        try {
            matchService.updateStandingsFromAPI();
            redirectAttributes.addFlashAttribute("successMessage", "Ranking successfully updated by API");
            System.out.println("Manual ranking update successfully completed");
        } catch (Exception e) {
            System.err.println("Error during manual ranking update: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error while updating the ranking: " + e.getMessage());
        }

        return "redirect:/admin/matches";
    }

    // Endpoint per aggiornare sia partite che classifica
    @PostMapping("/update-all")
    public String updateAll(RedirectAttributes redirectAttributes) {
        System.out.println("Request for complete update of matches and standings");

        try {
            matchService.updateMatchesFromAPI();
            matchService.updateStandingsFromAPI();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Matches and rankings successfully updated from the API");
            System.out.println("Full update successfully completed");
        } catch (Exception e) {
            System.err.println("Error during full update: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error during update: " + e.getMessage());
        }

        return "redirect:/admin/matches";
    }
}