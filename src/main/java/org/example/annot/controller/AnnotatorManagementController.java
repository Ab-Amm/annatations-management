package org.example.annot.controller;

import org.example.annot.model.Annotator;
import org.example.annot.repository.AnnotatorRepository;
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
    private AnnotatorRepository annotatorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String listAnnotators(Model model) {
        model.addAttribute("annotators", annotatorRepository.findByDeletedFalse());
        model.addAttribute("annotator", new Annotator());
        return "Admin/admin-annotators";
    }

    @PostMapping
    public String addAnnotator(@ModelAttribute Annotator annotator) {
        String rawPassword = generateRandomPassword(8);
        annotator.setPassword(passwordEncoder.encode(rawPassword));

        annotator.setRole(Role.ROLE_ANNOTATOR);
        annotator.setDeleted(false);

        annotatorRepository.save(annotator);

        System.out.println("Generated password for user " + annotator.getUsername() + ": " + rawPassword);

        return "redirect:/admin/annotators";
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }



    @GetMapping("/delete/{id}")
    public String softDelete(@PathVariable Long id) {
        annotatorRepository.findById(id).ifPresent(a -> {
            a.setDeleted(true);
            annotatorRepository.save(a);
        });
        return "redirect:/admin/annotators";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Annotator annotator = annotatorRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Invalid annotator ID: " + id)
        );
        model.addAttribute("annotator", annotator);
        return "Admin/annotator-edit";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Annotator annotator) {
        Annotator existing = annotatorRepository.findById(annotator.getId()).orElseThrow(
                () -> new IllegalArgumentException("Invalid annotator ID: " + annotator.getId())
        );
        existing.setFirstName(annotator.getFirstName());
        existing.setLastName(annotator.getLastName());
        annotatorRepository.save(existing);
        return "redirect:/admin/annotators";
    }

}
