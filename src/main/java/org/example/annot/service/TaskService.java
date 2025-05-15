package org.example.annot.service;

import org.example.annot.model.Annotator;
import org.example.annot.model.CoupleText;
import org.example.annot.model.Dataset;
import org.example.annot.model.Task;
import org.example.annot.repository.AnnotatorRepository;
import org.example.annot.repository.CoupleTextRepository;
import org.example.annot.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private CoupleTextRepository coupleTextRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AnnotatorRepository annotatorRepository;

    public void assignTextsToAnnotators(Dataset dataset, List<Annotator> annotators, LocalDate deadline) {
        List<CoupleText> allTexts = coupleTextRepository.findByDataset(dataset);


        Collections.shuffle(allTexts);

        int totalAnnotators = annotators.size();
        int chunkSize = allTexts.size() / totalAnnotators;
        int remainder = allTexts.size() % totalAnnotators;

        int index = 0;

        for (int i = 0; i < totalAnnotators; i++) {
            Annotator annotator = annotators.get(i);

            int currentChunkSize = chunkSize + (i < remainder ? 1 : 0); // distribute the remainder
            List<CoupleText> assignedTexts = allTexts.subList(index, index + currentChunkSize);

            Task task = new Task();
            task.setAnnotator(annotator);
            dataset.setAnnotated(true);
            task.setDataset(dataset);
            task.setDeadline(deadline);
            task.setCoupleTexts(new HashSet<>(assignedTexts));

            taskRepository.save(task);

            index += currentChunkSize;
        }
    }
}

