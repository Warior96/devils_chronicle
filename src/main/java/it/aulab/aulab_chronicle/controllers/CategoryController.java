package it.aulab.aulab_chronicle.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.dtos.CategoryDto;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.services.ArticleService;
import it.aulab.aulab_chronicle.services.CategoryService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ModelMapper modelMapper;

    // rotta get per ricerca articoli per categoria
    @GetMapping("/search/{id}")
    public String categorySearch(@PathVariable("id") Long id, Model viewModel) {

        CategoryDto category = categoryService.read(id);
        viewModel.addAttribute("title", "All articles found in " + category.getName() + " category");

        List<ArticleDto> articles = articleService.searchByCategory(modelMapper.map(category, Category.class));
        viewModel.addAttribute("articles", articles);

        return "article/articles";
    }

    // rotta get per creazione categoria
    @GetMapping("create")
    public String categoryCreate(Model viewModel) {
        viewModel.addAttribute("title", "Create Category");
        viewModel.addAttribute("category", new Category());
        return "category/create";
    }

    // rotta post per creazione categoria
    @PostMapping
    public String categoryStore(@Valid @ModelAttribute("category") Category category, BindingResult result,
            RedirectAttributes redirectAttributes, Model viewModel) {

        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Create Category");
            viewModel.addAttribute("category", category);
            return "category/create";
        }
        categoryService.create(category, null, null);
        redirectAttributes.addFlashAttribute("successMessage", "Category successfully created");
        return "redirect:/admin/dashboard";
    }

    // rotta get per modifica categoria
    @GetMapping("/edit/{id}")
    public String categoryEdit(@PathVariable("id") Long id, Model viewModel) {
        viewModel.addAttribute("title", "Edit Category");
        viewModel.addAttribute("category", categoryService.read(id));
        return "category/update";
    }

    // rotta post per modifica categoria
    @PostMapping("/update/{id}")
    public String categoryUpdate(@PathVariable("id") Long id, @Valid @ModelAttribute("category") Category category,
            BindingResult result, RedirectAttributes redirectAttributes, Model viewModel) {

        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Edit Category");
            viewModel.addAttribute("category", category);
            return "category/update";
        }

        categoryService.update(id, category, null);
        redirectAttributes.addFlashAttribute("successMessage", "Category successfully updated");

        return "redirect:/admin/dashboard";
    }

    // rotta get per eliminazione categoria
    @GetMapping("/delete/{id}")
    public String categoryDelete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {

        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Category successfully deleted");
        
        return "redirect:/admin/dashboard";

    }

}
