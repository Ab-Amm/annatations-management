package org.example.annot.controller;

import org.example.annot.model.Annotator;
import org.example.annot.model.ClassePossible;
import org.example.annot.model.CoupleText;
import org.example.annot.model.Dataset;
import org.example.annot.repository.AnnotatorRepository;
import org.example.annot.repository.ClassePossibleRepository;
import org.example.annot.repository.CoupleTextRepository;
import org.example.annot.repository.DatasetRepository;
import org.example.annot.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/datasets")
public class DatasetController {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private ClassePossibleRepository classePossibleRepository;

    @Autowired
    private CoupleTextRepository coupleTextRepository;

    @Autowired
    private AnnotatorRepository annotatorRepository;

    @Autowired
    private TaskService taskService;

    @GetMapping
    public String showDatasets(Model model) {
        List<Dataset> datasets = datasetRepository.findAll();
        model.addAttribute("datasets", datasets);
        return "Admin/all-datasets";
    }

    @GetMapping("/add")
    public String showAddDatasetForm(Model model) {
        model.addAttribute("dataset", new Dataset());
        return "Admin/add-dataset";
    }
    @PostMapping("/add")
    public String handleDatasetUpload(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("classes") String classes,
            @RequestParam("file") MultipartFile file) throws IOException {

        // Save Dataset
        Dataset dataset = new Dataset();
        dataset.setName(name);
        dataset.setDescription(description);
        dataset = datasetRepository.save(dataset);

        // Save ClassesPossible
        String[] classArray = classes.split(";");
        for (String c : classArray) {
            ClassePossible cp = new ClassePossible();
            cp.setTouteClasse(c.trim());
            cp.setDataset(dataset);
            classePossibleRepository.save(cp);
        }

        // Parse CSV and save CoupleText
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",", 2);
            if (parts.length == 2) {
                CoupleText couple = new CoupleText(parts[0].trim(), parts[1].trim(), dataset);
                coupleTextRepository.save(couple);
            }
        }
        return "redirect:/admin/datasets";
    }

    @GetMapping("/assign/{id}")
    public String showAnnotatorSelectionForm(@PathVariable Long id, Model model) {
        Dataset dataset = datasetRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Invalid dataset ID: " + id)
        );
        List<Annotator> annotators = annotatorRepository.findByDeletedFalse();
        model.addAttribute("dataset", dataset);
        model.addAttribute("annotators", annotators);
        System.out.println("Annotators test get : " + annotators);
        return "Admin/assign-annotators";
    }

    @PostMapping("/assign")
    public String assignAnnotatorsToDataset(
            @RequestParam Long datasetId,
            @RequestParam List<Long> annotatorIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline) {

        Dataset dataset = datasetRepository.findById(datasetId).orElseThrow();
        List<Annotator> annotators = annotatorRepository.findAllById(annotatorIds);

        taskService.assignTextsToAnnotators(dataset, annotators, deadline);

        return "redirect:/admin/datasets";
    }
}



