package org.example.annot.controller;

import org.example.annot.model.Annotator;
import org.example.annot.service.AnnotatorManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin/annotators")
public class AnnotatorManagementController {

    @Autowired
    private AnnotatorManagementService annotatorManagementService;

    @GetMapping
    public String listAnnotators(Model model) {
        model.addAttribute("annotator", new Annotator());
        System.out.println("Annotators test get : " + annotatorManagementService.findAllAnnotators());
        model.addAttribute("annotators", annotatorManagementService.findAllAnnotators());
        return "Admin/admin-annotators";
    }

    @PostMapping
    public String addAnnotator(@ModelAttribute Annotator annotator, RedirectAttributes redirectAttributes) {
        try {
            annotatorManagementService.addAnnotator(annotator);
            System.out.println("Generated password for user " + annotator.getUsername() + ": " + annotator.getPassword());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            System.out.println("error message: " + e.getMessage());
            return "redirect:/admin/annotators";
        }
        return "redirect:/admin/annotators";
    }

    @GetMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id) {
        annotatorManagementService.softDeleteAnnotator(id);
        return "redirect:/admin/annotators";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Annotator annotator = annotatorManagementService.findAnnotatorById(id);
        model.addAttribute("annotator", annotator);
        return "Admin/annotator-edit";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Annotator annotator) {
        annotatorManagementService.updateAnnotator(annotator);
        return "redirect:/admin/annotators";
    }
}

