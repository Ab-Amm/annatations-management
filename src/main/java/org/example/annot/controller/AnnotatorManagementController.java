package org.example.annot.controller;

import org.example.annot.model.Annotator;
import org.example.annot.repository.AnnotatorRepository;
import org.example.annot.service.AnnotatorManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.example.annot.model.Role;

import java.security.SecureRandom;

@Controller
@RequestMapping("/admin/annotators")
public class AnnotatorManagementController {

    @Autowired
    private AnnotatorManagementService annotatorManagementService;

    @GetMapping
    public String listAnnotators(Model model) {
        model.addAttribute("annotators", annotatorManagementService.findAllAnnotators());  // You might add a method in service for this.
        model.addAttribute("annotator", new Annotator());
        return "Admin/admin-annotators";
    }

    @PostMapping
    public String addAnnotator(@ModelAttribute Annotator annotator, Model model) {
        try {
            annotatorManagementService.addAnnotator(annotator);
            System.out.println("Generated password for user " + annotator.getUsername() + ": " + annotator.getPassword());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "Admin/admin-annotators"; // Return the same view to show the error
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

