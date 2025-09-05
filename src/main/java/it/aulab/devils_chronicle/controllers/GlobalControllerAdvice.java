package it.aulab.devils_chronicle.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import it.aulab.devils_chronicle.dtos.CategoryDto;
import it.aulab.devils_chronicle.services.CategoryService;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CategoryService categoryService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        try {
            List<CategoryDto> categories = categoryService.readAll();
            model.addAttribute("categories", categories);
        } catch (Exception e) {
            // lista vuota in caso di errori
            model.addAttribute("categories", List.of());
        }
    }
}