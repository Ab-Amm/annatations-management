package org.example.annot.controller;

import org.example.annot.service.AnnotationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/annotator/stats")
public class AnnotatorStatsController {

    private final AnnotationService annotationService;

    public AnnotatorStatsController(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @GetMapping("/annotator/stats")
    public String getAnnotatorStats(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();


        return "annotator/stats";
    }
}
