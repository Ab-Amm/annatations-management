package org.example.annot.controller;

import org.example.annot.model.Annotator;
import org.example.annot.model.AnnotatorStats;
import org.example.annot.model.Dataset;
import org.example.annot.model.Task;
import org.example.annot.repository.AnnotationRepository;
import org.example.annot.repository.AnnotatorRepository;
import org.example.annot.service.AnnotationService;
import org.example.annot.service.AnnotatorStatsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/annotator/stats")
public class AnnotatorStatsController {

    private final AnnotatorStatsService statsService;
    private final AnnotatorRepository annotatorRepository;



    public AnnotatorStatsController(AnnotatorStatsService statsService, AnnotatorRepository annotatorRepository) {
        this.statsService = statsService;
        this.annotatorRepository = annotatorRepository;
    }

    @GetMapping
    public String getMyStats(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Annotator annotator = annotatorRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Annotator not found"));

        AnnotatorStats stats = statsService.getAnnotatorStats(annotator.getId());

        List<Dataset> datasetsWorked = annotator.getTasks().stream().map(Task::getDataset).toList();
        model.addAttribute("datasetsWorked", datasetsWorked);

        model.addAttribute("annotator", annotator);
        model.addAttribute("stats", stats);
        return "annotator/my-stats";
    }
}
