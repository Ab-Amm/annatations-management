package org.example.annot.service;


import org.example.annot.model.Annotation;
import org.example.annot.model.Annotator;
import org.example.annot.model.CoupleText;
import org.example.annot.repository.AnnotationRepository;
import org.example.annot.repository.CoupleTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnotationService {

    private final AnnotationRepository annotationRepository;
    private final CoupleTextRepository coupleTextRepository;

    @Autowired
    public AnnotationService(AnnotationRepository annotationRepository, CoupleTextRepository coupleTextRepository) {
        this.annotationRepository = annotationRepository;
        this.coupleTextRepository = coupleTextRepository;
    }


    public Annotation getAnnotationForCouple(Long coupleTextId) {
        return annotationRepository.findByCoupleTextId(coupleTextId)
                .orElse(null);
    }


    public void saveAnnotation(Annotation annotation) {
        annotationRepository.save(annotation);
    }

    public int getNextIndex(Long taskId) {
        List<CoupleText> couples = coupleTextRepository.findByTaskIdOrderById(taskId);

        for(int i = 0; i < couples.size(); i++) {
            if(!couples.get(i).isDone()) {
                return i;
            }
        }
        return -1;
    }

    public Annotation createAnnotation(CoupleText coupleText, Annotator annotator, String chosenClass) {
        if (coupleText.getAnnotation() != null) {
            throw new IllegalStateException("This couple text is already annotated");
        }

        Annotation annotation = new Annotation();
        annotation.setChosenClass(chosenClass);
        annotation.setCoupleText(coupleText);

        coupleText.setAnnotation(annotation);

        return annotationRepository.save(annotation);
    }


    public void updateAnnotation(Long id, String chosenClass) {
        Annotation annotation = annotationRepository.findById(id).orElse(null);
        annotation.setChosenClass(chosenClass);
        annotationRepository.save(annotation);
    }
}
