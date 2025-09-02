package it.aulab.devils_chronicle.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
