package org.example.annot.controller;

import jakarta.persistence.EntityNotFoundException;
import org.example.annot.model.Annotation;
import org.example.annot.service.AnnotationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/annotations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnnotationController {

    private final AnnotationService annotationService;

    public AdminAnnotationController(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAnnotation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        try {
            annotationService.updateAnnotation(id, request.get("chosenClass"));
            System.out.println("Annotation updated successfully");
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            System.out.println("Annotation not found" + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}