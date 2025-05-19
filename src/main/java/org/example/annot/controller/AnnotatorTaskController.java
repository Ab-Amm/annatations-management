package org.example.annot.controller;


import org.example.annot.model.*;
import org.example.annot.repository.*;
import org.example.annot.service.AnnotationService;
import org.example.annot.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/annotator/tasks")
public class AnnotatorTaskController {


    private final TaskService taskService;

    private final AnnotatorRepository annotatorRepository;

    private final AnnotationRepository annotationRepository;
    private final AnnotationService annotationService;
    private final CoupleTextRepository coupleTextRepository;

    @Autowired
    public AnnotatorTaskController(TaskService taskService, AnnotatorRepository annotatorRepository, AnnotationRepository annotationRepository, AnnotationService annotationService, CoupleTextRepository coupleTextRepository) {
        this.taskService = taskService;
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
        return "annotator/all-tasks";
    }

    @GetMapping("/annotateTask/{taskId}")
    public String getAnnotationPage(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int currentIndex,
            Model model) {

        Task task = taskService.getTaskWithCouples(taskId);
        List<CoupleText> couples = task.getCoupleTexts();

        List<CoupleText> notDoneCouples = couples.stream()
                .filter(c -> !c.isDone())
                .toList();

        Annotator annotator = annotatorRepository.findByUsername(
                        SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UsernameNotFoundException("Annotator not found"));

        currentIndex = Math.max(0, Math.min(currentIndex, notDoneCouples.size() - 1));

        double progressPercentage = couples.isEmpty() ? 0 :
                ((double) task.getCouplesDone() / couples.size()) * 100;

        CoupleText currentCouple = notDoneCouples.get(currentIndex);
        Annotation annotation = annotationService.getAnnotationForCoupleAndAnnotator(currentCouple.getId(), annotator.getId());
        model.addAttribute("task", task);
        model.addAttribute("currentCouple", currentCouple);
        model.addAttribute("currentIndex", currentIndex);
        model.addAttribute("totalCouples", couples.size());
        model.addAttribute("progressPercentage", progressPercentage);
        model.addAttribute("annotator", annotator);
        model.addAttribute("currentAnnotation", annotation);

        return "annotator/annotate-task";
    }





    @PostMapping("/annotateTask/{taskId}")
    public String handleAnnotation(
            @PathVariable Long taskId,
            @RequestParam Long coupleId,
            @RequestParam String selectedClass,
            @RequestParam int currentIndex) {


        // get Annotator
        Annotator annotator = annotatorRepository.findByUsernameWithTasks(SecurityContextHolder.getContext()
                .getAuthentication()
                .getName())
                .orElseThrow(() -> new UsernameNotFoundException("Annotator not found"));

        CoupleText coupleText = taskService.getCoupleText(coupleId);
        if (!coupleText.isDone()) {
            Annotation annotation = annotationRepository.findByCoupleTextIdAndAnnotatorId(coupleId, annotator.getId())
                    .orElse(new Annotation());
            annotation.setAnnotator(annotator);
            coupleText.setDone(true);
            coupleTextRepository.save(coupleText);
            annotation.setCoupleText(coupleText);
            annotation.setChosenClass(selectedClass);
            annotationService.saveAnnotation(annotation);


            // Update task progress
            Task task = coupleText.getTask();
            task.setCouplesDone(task.getCouplesDone() + 1);
            if(task.getCouplesDone() == task.getCoupleTexts().size()) {
                task.setAllCouplesDone(true);
            }
            taskService.saveTask(task);
        }

        return "redirect:/annotator/tasks/annotateTask/" + taskId + "?currentIndex=" + currentIndex;
    }

    @GetMapping("/annotateTask/{taskId}/navigate")
    public String navigate(
            @PathVariable Long taskId,
            @RequestParam int newIndex) {
        return "redirect:/annotator/tasks/annotateTask/" + taskId + "?currentIndex=" + newIndex;
    }




}
