package it.aulab.aulab_chronicle.controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.dtos.UserDto;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.CareerRequestRepository;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.CategoryService;
import it.aulab.aulab_chronicle.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CareerRequestRepository careerRequestRepository;

    @Autowired
    private CategoryService categoryService;

    // rotta get per la registrazione
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserDto());
        return "auth/register";
    }

    // rotta post per la registrazione
    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto, BindingResult result, Model model,
            RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {

        User existingUser = userService.findUserByEmail(userDto.getEmail());

        if (existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()) {
            result.rejectValue("email", null, "There is already an account registered with the email provided");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "auth/register";
        }

        userService.saveUser(userDto, redirectAttributes, request, response);

        redirectAttributes.addFlashAttribute("successMessage", "User successfully registered");

        return "redirect:/";
    }

    // rotta get per il login
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // rotta get per la home
    @GetMapping("/")
    public String home(Model viewModel) {

        List<ArticleDto> articles = articleService.readAll();

        // ordine di visualizzazione articoli
        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());

        List<ArticleDto> lastThreeArticles = articles.stream().limit(3).collect(Collectors.toList());

        viewModel.addAttribute("articles", lastThreeArticles);

        return "home";
    }

    // rotta get per la ricerca articoli per utente
    @GetMapping("/search/{id}")
    public String userArticlesSearch(@PathVariable("id") Long id, Model viewModel) {

        User user = userService.find(id);
        viewModel.addAttribute("title", "All articles found by " + user.getUsername() + " user");

        List<ArticleDto> articles = articleService.searchByAuthor(user);
        viewModel.addAttribute("articles", articles);

        return "article/articles";
    }

    // rotta get per la dashboard
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model viewModel) {

        viewModel.addAttribute("title", "Admin Dashboard");
        
        viewModel.addAttribute("pendingRequests", careerRequestRepository.findByIsAcceptedIsNull());
        viewModel.addAttribute("acceptedRequests", careerRequestRepository.findByIsAcceptedTrue());
        viewModel.addAttribute("rejectedRequests", careerRequestRepository.findByIsAcceptedFalse());

        viewModel.addAttribute("categories", categoryService.readAll());

        return "admin/dashboard";

    }

}
