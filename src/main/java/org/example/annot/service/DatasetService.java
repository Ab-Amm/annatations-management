package org.example.annot.service;


import jakarta.persistence.EntityNotFoundException;
import org.example.annot.model.Dataset;
import org.example.annot.repository.DatasetRepository;
import org.springframework.stereotype.Service;

@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;

    public DatasetService(DatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    public DatasetRepository getDatasetRepository() {
        return datasetRepository;
    }

    public void saveDataset(Dataset dataset) {
        datasetRepository.save(dataset);
    }

    public Dataset getDatasetWithAnnotations(Long id) {
        return datasetRepository.findByIdWithAnnotations(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));
    }
}
