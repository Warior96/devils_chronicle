package it.aulab.devils_chronicle.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.devils_chronicle.dtos.ArticleDto;
import it.aulab.devils_chronicle.dtos.UserDto;
import it.aulab.devils_chronicle.models.Article;
import it.aulab.devils_chronicle.models.User;
import it.aulab.devils_chronicle.repositories.ArticleRepository;
import it.aulab.devils_chronicle.repositories.CareerRequestRepository;
import it.aulab.devils_chronicle.services.ArticleService;
import it.aulab.devils_chronicle.services.CategoryService;
import it.aulab.devils_chronicle.services.UserService;

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

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ModelMapper modelMapper;

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

        List<ArticleDto> articles = new ArrayList<ArticleDto>();
        for (Article article : articleRepository.findByIsAcceptedTrue()) {
            articles.add(modelMapper.map(article, ArticleDto.class));
        }

        // ordine di visualizzazione articoli
        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());

        List<ArticleDto> lastThreeArticles = articles.stream().limit(4).collect(Collectors.toList());

        viewModel.addAttribute("articles", lastThreeArticles);

        return "home";
    }

    // rotta get per la ricerca articoli per utente
    @GetMapping("/search/{id}")
    public String userArticlesSearch(@PathVariable("id") Long id, Model viewModel) {

        User user = userService.find(id);
        viewModel.addAttribute("title", "All articles found by " + user.getUsername() + " user");

        List<ArticleDto> articles = articleService.searchByAuthor(user);

        List<ArticleDto> acceptedArticles = articles.stream()
                .filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).toList();

        viewModel.addAttribute("keyword", "author: " + user.getUsername());

        viewModel.addAttribute("articles", acceptedArticles);

        return "article/articles";
    }

    // rotta get per la dashboard amministratore
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model viewModel) {

        viewModel.addAttribute("title", "Admin Dashboard");

        viewModel.addAttribute("pendingRequests", careerRequestRepository.findByIsAcceptedIsNull());
        viewModel.addAttribute("acceptedRequests", careerRequestRepository.findByIsAcceptedTrue());
        viewModel.addAttribute("rejectedRequests", careerRequestRepository.findByIsAcceptedFalse());

        viewModel.addAttribute("categories", categoryService.readAll());

        return "admin/dashboard";

    }

    // rotta get per la dashboard revisore
    @GetMapping("/revisor/dashboard")
    public String revisorDashboard(Model viewModel) {

        viewModel.addAttribute("title", "Revisor Dashboard");

        viewModel.addAttribute("pendingArticles", articleRepository.findByIsAcceptedNull());
        viewModel.addAttribute("acceptedArticles", articleRepository.findByIsAcceptedTrue());
        viewModel.addAttribute("rejectedArticles", articleRepository.findByIsAcceptedFalse());

        return "revisor/dashboard";
    }

    //
    @GetMapping("/writer/dashboard")
    public String writerDashboard(Model viewModel, Principal principal) {

        viewModel.addAttribute("title", "Writer Dashboard: your articles");

        List<ArticleDto> userArticles = articleService.readAll().stream()
                .filter(article -> article.getUser().getEmail().equals(principal.getName()))
                .toList();

        List<ArticleDto> acceptedArticles = userArticles.stream()
                .filter(article -> Boolean.TRUE.equals(article.getIsAccepted()))
                .toList();

        List<ArticleDto> rejectedArticles = userArticles.stream()
                .filter(article -> Boolean.FALSE.equals(article.getIsAccepted()))
                .toList();

        List<ArticleDto> pendingArticles = userArticles.stream()
                .filter(article -> article.getIsAccepted() == null)
                .toList();

        viewModel.addAttribute("acceptedArticles", acceptedArticles);
        viewModel.addAttribute("rejectedArticles", rejectedArticles);
        viewModel.addAttribute("pendingArticles", pendingArticles);

        viewModel.addAttribute("articles", userArticles);

        return "writer/dashboard";
    }

}
