package it.aulab.aulab_chronicle.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.CategoryDto;
import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.CrudService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    @Qualifier("categoryService") // qualifier per specificare quale service utilizzare
    private CrudService<CategoryDto, Category, Long> categoryService;

    @Autowired
    private ArticleService articleService;

    // rotta get per creazione articolo
    @GetMapping("create")
    public String artcileCreate(Model viewModel) {
        viewModel.addAttribute("title", "Create Article");
        viewModel.addAttribute("article", new Article());
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/create";
    }

    // rotta post per creazione articolo
    @PostMapping
    public String articleStore(
            @Valid @ModelAttribute("article") Article article, BindingResult result,
            RedirectAttributes redirectAttributes, Principal principal, MultipartFile file, Model model) {

        // validazione
        if (result.hasErrors()) {
            model.addAttribute("title", "Create Article");
            model.addAttribute("article", article);
            model.addAttribute("categories", categoryService.readAll());
            return "article/create";
        }

        articleService.create(article, principal, file);
        redirectAttributes.addFlashAttribute("successMessage", "Article successfully created");
        return "redirect:/";

    }

}
