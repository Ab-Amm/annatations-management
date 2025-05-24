package org.example.annot.service;



import jakarta.persistence.EntityNotFoundException;
import org.example.annot.model.Annotator;
import org.example.annot.model.Role;
import org.example.annot.repository.AnnotatorRepository;
import org.example.annot.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class AnnotatorManagementService {

    @Autowired
    private AnnotatorRepository annotatorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TaskRepository taskRepository;

    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    public void addAnnotator(Annotator annotator) {

        if (annotatorRepository.existsByUsername(annotator.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        String rawPassword = generateRandomPassword(8);
        System.out.println("Generated raw password for user " + annotator.getUsername() + ": " + rawPassword);
        annotator.setPassword(passwordEncoder.encode(rawPassword));
        annotator.setRole(Role.ROLE_ANNOTATOR);
        annotator.setDeleted(false);
        annotatorRepository.save(annotator);
    }

    public void softDeleteAnnotator(Long id) {
        Annotator annotator = annotatorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annotator not found"));

        if (hasUnfinishedTasks(id)) {
            throw new IllegalStateException("Annotator has unfinished tasks");
        }

        annotator.setDeleted(true);
        annotatorRepository.save(annotator);
    }

    public boolean hasUnfinishedTasks(Long annotatorId) {
        return taskRepository.existsByAnnotatorIdAndAllCouplesDoneFalse(annotatorId);
    }

    public void updateAnnotator(Annotator annotator) {
        Annotator existing = annotatorRepository.findById(annotator.getId()).orElseThrow(
                () -> new IllegalArgumentException("Invalid annotator ID: " + annotator.getId())
        );
        existing.setFirstName(annotator.getFirstName());
        existing.setLastName(annotator.getLastName());
        annotatorRepository.save(existing);
    }


    public Annotator findAnnotatorById(Long id) {
        return annotatorRepository.findById(id).orElse(null);
    }

    public List<Annotator> findAllAnnotators() {
        return annotatorRepository.findByDeletedFalse();
    }


}

