package com.schemadrift.detector.service;

import com.schemadrift.detector.model.*;
import com.schemadrift.detector.repository.ComparisonRepository;
import com.schemadrift.detector.repository.SchemaSnapshotRepository;
import com.schemadrift.detector.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComparisonService {

    private final SourceRepository sourceRepository;
    private final SchemaSnapshotRepository snapshotRepository;
    private final ComparisonRepository comparisonRepository;
    private final DiffEngineService diffEngineService;
    private final GeminiExplanationService geminiExplanationService;

    @Autowired
    public ComparisonService(SourceRepository sourceRepository,
                              SchemaSnapshotRepository snapshotRepository,
                              ComparisonRepository comparisonRepository,
                              DiffEngineService diffEngineService,
                              GeminiExplanationService geminiExplanationService) {
        this.sourceRepository = sourceRepository;
        this.snapshotRepository = snapshotRepository;
        this.comparisonRepository = comparisonRepository;
        this.diffEngineService = diffEngineService;
        this.geminiExplanationService = geminiExplanationService;
    }

    /**
     * Runs a full comparison between two snapshot IDs and persists the result.
     * AI explanations are only requested for BREAKING changes, to keep API
     * usage low and because safe changes don't need justification.
     */
    public Comparison runComparison(Long sourceId, Long oldSnapshotId, Long newSnapshotId) {
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));
        SchemaSnapshot oldSnapshot = snapshotRepository.findById(oldSnapshotId)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot not found: " + oldSnapshotId));
        SchemaSnapshot newSnapshot = snapshotRepository.findById(newSnapshotId)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot not found: " + newSnapshotId));

        Comparison comparison = diffEngineService.compare(source, oldSnapshot, newSnapshot);

        for (FieldChange change : comparison.getChanges()) {
            if (change.getSeverity() == Severity.BREAKING) {
                String aiExplanation = geminiExplanationService.explain(
                        change.getFieldName(),
                        change.getChangeType().name(),
                        change.getOldValue(),
                        change.getNewValue());
                if (aiExplanation != null && !aiExplanation.isBlank()) {
                    change.setAiExplanation(aiExplanation);
                }
                // if AI call fails, the rule-based reason set by DiffEngineService remains
            }
        }

        return comparisonRepository.save(comparison);
    }
}
