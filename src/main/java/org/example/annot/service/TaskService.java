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

        List<CoupleText> allCouples = coupleTextRepository.findByDataset(dataset);


        int annotatorCount = annotators.size();
        int batchSize = allCouples.size() / annotatorCount;
        int remaining = allCouples.size() % annotatorCount;
        int startIndex = 0;

        for (int i = 0; i < annotators.size(); i++) {
            int endIndex = startIndex + batchSize + (i < remaining ? 1 : 0);
            List<CoupleText> subList = allCouples.subList(startIndex, endIndex);


            Task task = new Task();
            dataset.setAnnotated(true);
            task.setDataset(dataset);
            task.setAnnotator(annotators.get(i));
            task.setDeadline(deadline);
            task.setPercentageDone(0);
            task = taskRepository.save(task);


            for (CoupleText ct : subList) {
                ct.setTask(task);
                coupleTextRepository.save(ct);
            }

            startIndex = endIndex;
        }
    }
}

