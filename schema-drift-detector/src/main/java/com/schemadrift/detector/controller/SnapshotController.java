package com.schemadrift.detector.controller;

import com.schemadrift.detector.dto.CaptureSnapshotRequest;
import com.schemadrift.detector.model.SchemaSnapshot;
import com.schemadrift.detector.model.Source;
import com.schemadrift.detector.repository.SchemaSnapshotRepository;
import com.schemadrift.detector.repository.SourceRepository;
import com.schemadrift.detector.service.SchemaCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources/{sourceId}/snapshots")
public class SnapshotController {

    private final SourceRepository sourceRepository;
    private final SchemaSnapshotRepository snapshotRepository;
    private final SchemaCaptureService captureService;

    @Autowired
    public SnapshotController(SourceRepository sourceRepository,
                               SchemaSnapshotRepository snapshotRepository,
                               SchemaCaptureService captureService) {
        this.sourceRepository = sourceRepository;
        this.snapshotRepository = snapshotRepository;
        this.captureService = captureService;
    }

    @GetMapping
    public List<SchemaSnapshot> listSnapshots(@PathVariable Long sourceId) {
        return snapshotRepository.findBySourceIdOrderByCapturedAtDesc(sourceId);
    }

    @PostMapping
    public ResponseEntity<?> captureSnapshot(@PathVariable Long sourceId,
                                              @RequestBody CaptureSnapshotRequest request) throws Exception {
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + sourceId));

        SchemaSnapshot snapshot = captureService.captureFromMySql(
                source, request.getJdbcUrl(), request.getUsername(),
                request.getPassword(), request.getTableName());

        return ResponseEntity.ok(snapshot);
    }
}
