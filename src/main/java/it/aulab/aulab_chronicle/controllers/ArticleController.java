package it.aulab.aulab_chronicle.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.dtos.CategoryDto;
import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.CrudService;
import it.aulab.aulab_chronicle.services.GeminiService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    @Qualifier("categoryService") // qualifier per specificare quale service utilizzare
    private CrudService<CategoryDto, Category, Long> categoryService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private GeminiService geminiService;

    // rotta get per visualizzazione articoli
    @GetMapping
    public String articlesIndex(Model viewModel) {

        viewModel.addAttribute("title", "All articles");

        List<ArticleDto> articles = new ArrayList<ArticleDto>();
        for (Article article : articleRepository.findByIsAcceptedTrue()) {
            articles.add(modelMapper.map(article, ArticleDto.class));
        }

        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());
        viewModel.addAttribute("articles", articles);

        return "article/articles";

    }

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
            RedirectAttributes redirectAttributes, Principal principal, @RequestParam("file") MultipartFile file,
            @RequestParam(value = "galleryFiles", required = false) MultipartFile[] galleryFiles, Model model) {

        // validazione
        if (result.hasErrors()) {
            model.addAttribute("title", "Create Article");
            model.addAttribute("article", article);
            model.addAttribute("categories", categoryService.readAll());
            model.addAttribute("today", LocalDate.now());
            return "article/create";
        }

        articleService.create(article, principal, file, galleryFiles);
        redirectAttributes.addFlashAttribute("successMessage", "Article successfully created, awaiting approval");
        return "redirect:/";

    }

    // rotta get per visualizzazione dettaglio articolo
    @GetMapping("detail/{id}")
    public String detailArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article Detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "article/detail";
    }

    // rotta get per modifica articolo
    @GetMapping("/edit/{id}")
    public String editArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Edit Article");
        viewModel.addAttribute("article", articleService.read(id));
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/edit";
    }

    // rotta post per modifica articolo
    @PostMapping("/update/{id}")
    public String articleUpdate(@PathVariable("id") Long id, @Valid @ModelAttribute("article") Article article,
            BindingResult result, RedirectAttributes redirectAttributes, Principal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "galleryFiles", required = false) MultipartFile[] galleryFiles,
            Model viewModel) {

        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Edit Article");
            article.setImage(articleService.read(id).getImage());
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/edit";
        }

        articleService.update(id, article, file, galleryFiles);

        redirectAttributes.addFlashAttribute("successMessage", "Article successfully updated, awaiting approval");
        return "redirect:/";
    }

    // rotta get per eliminazione articolo
    @GetMapping("/delete/{id}")
    public String articleDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        articleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Article successfully deleted");
        return "redirect:/writer/dashboard";
    }

    // rotta get per revisore visualizzazione dettaglio articolo
    @GetMapping("/revisor/detail/{id}")
    public String revisorDetailArticle(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article Detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "revisor/detail";
    }

    // rotta post per accettazione o rifiuto articolo
    @PostMapping("/accept")
    public String articleSetAccepted(@RequestParam("action") String action, @RequestParam("articleId") Long articleId,
            RedirectAttributes redirectAttributes) {

        if (action.equals("accept")) {
            articleService.setIsAccepted(true, articleId);
            redirectAttributes.addFlashAttribute("successMessage", "Article successfully accepted");
        } else if (action.equals("reject")) {
            articleService.setIsAccepted(false, articleId);
            redirectAttributes.addFlashAttribute("successMessage", "Article rejected");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Invalid action");
        }

        return "redirect:/revisor/dashboard";
    }

    // rotta get per ricerca articoli
    @GetMapping("/search")
    public String articleSearch(@Param("keyword") String keyword, Model viewModel) {

        viewModel.addAttribute("title", "All searched articles for " + keyword);

        viewModel.addAttribute("keyword", keyword);

        List<ArticleDto> articles = articleService.search(keyword);

        List<ArticleDto> acceptedArticles = articles.stream()
                .filter(article -> Boolean.TRUE.equals(article.getIsAccepted())).collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);

        return "article/articles";

    }

    // rotta get per ottenere il riassunto di un articolo
    @GetMapping("/detail/{id}/summary")
    public String summarizeArticle(@PathVariable Long id, Model model) {
        ArticleDto article = articleService.read(id);
        String summary = geminiService.getSummary(article.getTitle(), article.getBody());

        model.addAttribute("article", article);
        model.addAttribute("summary", summary);

        return "article/detail";
    }

}
