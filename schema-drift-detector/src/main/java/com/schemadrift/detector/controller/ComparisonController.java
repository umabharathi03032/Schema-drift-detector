package com.schemadrift.detector.controller;

import com.schemadrift.detector.dto.CompareRequest;
import com.schemadrift.detector.model.Comparison;
import com.schemadrift.detector.repository.ComparisonRepository;
import com.schemadrift.detector.service.ComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comparisons")
public class ComparisonController {

    private final ComparisonService comparisonService;
    private final ComparisonRepository comparisonRepository;

    @Autowired
    public ComparisonController(ComparisonService comparisonService,
                                 ComparisonRepository comparisonRepository) {
        this.comparisonService = comparisonService;
        this.comparisonRepository = comparisonRepository;
    }

    @PostMapping
    public Comparison compare(@RequestBody CompareRequest request) {
        return comparisonService.runComparison(
                request.getSourceId(), request.getOldSnapshotId(), request.getNewSnapshotId());
    }

    @GetMapping("/by-source/{sourceId}")
    public List<Comparison> history(@PathVariable Long sourceId) {
        return comparisonRepository.findBySourceIdOrderByCreatedAtDesc(sourceId);
    }

    @GetMapping("/{id}")
    public Comparison detail(@PathVariable Long id) {
        return comparisonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comparison not found: " + id));
    }
}
