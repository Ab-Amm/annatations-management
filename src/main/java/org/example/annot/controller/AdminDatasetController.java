package org.example.annot.controller;

import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.annot.model.*;
import org.example.annot.repository.*;
import org.example.annot.service.AnnotationService;
import org.example.annot.service.AnnotatorStatsService;
import org.example.annot.service.DatasetService;
import org.example.annot.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/datasets")
public class AdminDatasetController {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private AnnotatorStatsService statsService;

    @Autowired
    private ClassePossibleRepository classePossibleRepository;

    @Autowired
    private CoupleTextRepository coupleTextRepository;

    @Autowired
    private AnnotatorRepository annotatorRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private AnnotationService annotationService;

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
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

        // Save Dataset
        Dataset dataset = new Dataset();
        dataset.setName(name);
        dataset.setDescription(description);
        dataset = datasetRepository.save(dataset);

        // Save ClassesPossible
        String[] classArray = classes.split(";");
        if (classArray.length < 2) {
            redirectAttributes.addFlashAttribute("error", "Please enter at least two classes separated by semicolons (;).");
            return "redirect:/admin/datasets/add";
        }
        for (String c : classArray) {
            ClassePossible cp = new ClassePossible();
            cp.setTouteClasse(c.trim());
            cp.setDataset(dataset);
            classePossibleRepository.save(cp);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

        int coupleCount = 0;

        for (CSVRecord record : csvParser) {
            if (record.size() >= 2) {
                String text1 = record.get(0).trim();
                String text2 = record.get(1).trim();
                CoupleText couple = new CoupleText(text1, text2, dataset);
                coupleTextRepository.save(couple);
                coupleCount++;
            }
        }
        csvParser.close();


        dataset.setTotalCouples(coupleCount);
        datasetRepository.save(dataset);
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

    @GetMapping("/details/{id}")
    public String getDatasetDetails(@PathVariable Long id,
                                    @RequestParam(defaultValue = "0") int page,
                                    Model model) {
        Dataset dataset = datasetRepository.findById(id).orElseThrow();
        int pageSize = 100;

        // Get paginated couples
        List<CoupleText> allCouples = coupleTextRepository.findByDataset(dataset);
        int totalCouples = allCouples.size();
        int totalPages = (int) Math.ceil((double) totalCouples / pageSize);
        List<CoupleText> couplesPage = allCouples.subList(
                page * pageSize,
                Math.min((page + 1) * pageSize, totalCouples)
        );

        // Calculate completion percentage
        long annotatedCount = allCouples.stream().filter(CoupleText::isDone).count();
        double completionPercentage = totalCouples > 0 ?
                (annotatedCount * 100.0) / totalCouples : 0.0;

        model.addAttribute("dataset", dataset);
        model.addAttribute("couplesPage", couplesPage);
        model.addAttribute("totalCouples", totalCouples);
        model.addAttribute("currentPage", page);
        model.addAttribute("completionPercentage", Math.round(completionPercentage));

        return "Admin/dataset-details";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid dataset ID: " + id));
        model.addAttribute("dataset", dataset);
        return "Admin/edit-dataset";
    }

    @PostMapping("/edit/{id}")
    public String updateDataset(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid dataset ID: " + id));
        dataset.setName(name);
        dataset.setDescription(description);
        datasetRepository.save(dataset);
        return "redirect:/admin/datasets";
    }

    @PostMapping("/delete/{id}")
    public String deleteDataset(@PathVariable Long id) {
        datasetRepository.deleteById(id);
        return "redirect:/admin/datasets";
    }

    @GetMapping("/export/{id}")
    public void exportDatasetAsCsv(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Dataset dataset = datasetService.getDatasetWithAnnotations(id);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=dataset-" + id + ".csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("text1,text2,annotation,annotator,annotation_date");

            for (CoupleText couple : dataset.getCoupleText()) {
                String annotation = "";
                String annotator = "";
                String annotationDate = "";
                if (couple.getAnnotation() != null) {
                    Annotation ann = couple.getAnnotation();
                    annotation = ann.getChosenClass();

                    // Get annotator username if available
                    if (ann.getAnnotator() != null) {
                        annotator = ann.getAnnotator().getUsername();
                    }

                    // Format date if available
                    if (ann.getCreationDate() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        annotationDate = sdf.format(ann.getCreationDate());
                    }
                }

                // Escape quotes and build CSV line
                writer.println(
                        "\"" + couple.getText1().replace("\"", "\"\"") + "\"," +
                                "\"" + couple.getText2().replace("\"", "\"\"") + "\"," +
                                "\"" + annotation.replace("\"", "\"\"") + "\"," +
                                "\"" + annotator.replace("\"", "\"\"") + "\"," +
                                "\"" + annotationDate.replace("\"", "\"\"") + "\""
                );
            }
        }
    }

    @GetMapping("/annotators/stats")
    public String listAnnotatorStats(Model model) {
        Map<Long, BasicStats> stats = statsService.getBasicStatsForAll();
        List<Annotator> annotators = annotatorRepository.findAll();

        model.addAttribute("annotators", annotators);
        model.addAttribute("statsMap", stats);
        List<Task> completedTasks = taskService.getCompletedTasks();

        model.addAttribute("completedTasks", completedTasks);
        Long averageTimeSpent = statsService.getAverageTimeSpent();
        model.addAttribute("averageTimeSpent", averageTimeSpent);

        long annotationsCount = annotationService.getAnnotationsCount();
        model.addAttribute("annotationsCount", annotationsCount);

        return "Admin/annotator-stats-list";
    }

    @GetMapping("/annotators/{id}/stats")
    public String getAnnotatorStats(@PathVariable Long id, Model model) {
        Annotator annotator = annotatorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annotator not found"));

        AnnotatorStats stats = statsService.getAnnotatorStats(id);

        model.addAttribute("annotator", annotator);
        model.addAttribute("stats", stats);
        return "Admin/annotator-statistics";
    }
}



