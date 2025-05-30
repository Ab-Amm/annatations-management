package org.example.annot.controller;


import jakarta.servlet.http.HttpSession;
import org.example.annot.model.*;
import org.example.annot.repository.*;
import org.example.annot.service.AnnotationService;
import org.example.annot.service.DatasetService;
import org.example.annot.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/annotator/tasks")
public class AnnotatorTaskController {


    private final TaskService taskService;
    private final DatasetService datasetService;

    private final AnnotatorRepository annotatorRepository;

    private final AnnotationRepository annotationRepository;
    private final AnnotationService annotationService;
    private final CoupleTextRepository coupleTextRepository;

    @Autowired
    public AnnotatorTaskController(TaskService taskService, DatasetService datasetService, AnnotatorRepository annotatorRepository, AnnotationRepository annotationRepository, AnnotationService annotationService, CoupleTextRepository coupleTextRepository) {
        this.taskService = taskService;
        this.datasetService = datasetService;
        this.annotatorRepository = annotatorRepository;
        this.annotationRepository = annotationRepository;
        this.annotationService = annotationService;
        this.coupleTextRepository = coupleTextRepository;
    }

    @GetMapping
    public String getAnnotatorTasks(Model model) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Annotator annotator = annotatorRepository.findByUsernameWithTasks(username)
                .orElseThrow(() -> new UsernameNotFoundException("Annotator not found"));

        model.addAttribute("annotator", annotator);
        model.addAttribute("pageTitle", "My Tasks");
        model.addAttribute("activeMenu", "tasks");

        return "annotator/all-tasks";
    }

    @GetMapping("/annotateTask/{taskId}")
    public String getAnnotationPage(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int currentIndex,
            Model model,
            HttpSession session) {

        Task task = taskService.getTaskWithCouples(taskId);
        List<CoupleText> couples = task.getCoupleTexts();


        Annotator annotator = annotatorRepository.findByUsername(
                        SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UsernameNotFoundException("Annotator not found"));

        currentIndex = Math.max(0, Math.min(currentIndex, couples.size() - 1));

        double progressPercentage = couples.isEmpty() ? 0 :
                ((double) task.getCouplesDone() / couples.size()) * 100;

        CoupleText currentCouple = couples.get(currentIndex);
        Annotation annotation = annotationService.getAnnotationForCouple(currentCouple.getId());
        model.addAttribute("task", task);
        model.addAttribute("currentCouple", currentCouple);
        model.addAttribute("currentIndex", currentIndex);
        model.addAttribute("totalCouples", couples.size());
        model.addAttribute("progressPercentage", progressPercentage);
        model.addAttribute("annotator", annotator);
        model.addAttribute("currentAnnotation", annotation);model.addAttribute("pageTitle", "Annotation Task");
        model.addAttribute("activeMenu", "tasks");

        model.addAttribute("nextUnannotatedIndex",
                taskService.findNextUnannotatedIndex(taskId, currentIndex));
        model.addAttribute("prevUnannotatedIndex",
                taskService.findPrevUnannotatedIndex(taskId, currentIndex));


        Long coupleId = currentCouple.getId();
        session.setAttribute("startTime_" + coupleId, System.currentTimeMillis());


        return "annotator/annotate-task";
    }

    @GetMapping("/annotateTask/{taskId}/navigateUnannotated")
    public String navigateUnannotated(
            @PathVariable Long taskId,
            @RequestParam int currentIndex,
            @RequestParam String direction,
            HttpSession session) {

        Integer newIndex = "next".equalsIgnoreCase(direction)
                ? taskService.findNextUnannotatedIndex(taskId, currentIndex)
                : taskService.findPrevUnannotatedIndex(taskId, currentIndex);

        if (newIndex == null) {
            newIndex = currentIndex; // Stay on current if none found
        }

        Task task = taskService.getTaskWithCouples(taskId);
        List<CoupleText> couples = task.getCoupleTexts();

        CoupleText currentCouple = couples.get(newIndex);
        Long coupleId = currentCouple.getId();
        session.setAttribute("startTime_" + coupleId, System.currentTimeMillis());

        return "redirect:/annotator/tasks/annotateTask/" + taskId + "?currentIndex=" + newIndex;
    }



    @PostMapping("/annotateTask/{taskId}")
    public String handleAnnotation(
            HttpSession session,
            @PathVariable Long taskId,
            @RequestParam Long coupleId,
            @RequestParam String selectedClass,
            @RequestParam int currentIndex) {

        CoupleText coupleText = taskService.getCoupleText(coupleId);
        if (!coupleText.isDone()) {
            long startTime = (Long) session.getAttribute("startTime_" + coupleId);
            long durationMillis = System.currentTimeMillis() - startTime;


            Annotation annotation = annotationRepository.findByCoupleTextId(coupleId)
                    .orElse(new Annotation());
            Duration duration = Duration.ofMillis(durationMillis);
            annotation.setTimeSpent(duration);
            coupleText.setDone(true);
            coupleTextRepository.save(coupleText);
            annotation.setCoupleText(coupleText);
            annotation.setChosenClass(selectedClass);

            Annotator annotator = annotatorRepository.findByUsername(
                            SecurityContextHolder.getContext().getAuthentication().getName())
                    .orElseThrow(() -> new UsernameNotFoundException("Annotator not found"));
            annotation.setAnnotator(annotator);

            annotationService.saveAnnotation(annotation);


            Task task = coupleText.getTask();
            Dataset dataset = task.getDataset();
            task.setCouplesDone(task.getCouplesDone() + 1);
            dataset.setCouplesDone(dataset.getCouplesDone() + 1);
            if(task.getCouplesDone() == task.getCoupleTexts().size()) {
                task.setAllCouplesDone(true);
            }

            taskService.saveTask(task);


            if (dataset.getCouplesDone() == dataset.getTotalCouples()) {
                dataset.setAnnotated(true);
            }

            datasetService.saveDataset(dataset);
        }
        currentIndex++;


        return "redirect:/annotator/tasks/annotateTask/" + taskId + "?currentIndex=" + currentIndex;
    }

    @GetMapping("/annotateTask/{taskId}/navigate")
    public String navigate(
            @PathVariable Long taskId,
            @RequestParam int newIndex) {
        return "redirect:/annotator/tasks/annotateTask/" + taskId + "?currentIndex=" + newIndex;
    }




}
