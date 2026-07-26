package com.schemadrift.detector.repository;

import com.schemadrift.detector.model.SchemaSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SchemaSnapshotRepository extends JpaRepository<SchemaSnapshot, Long> {
    List<SchemaSnapshot> findBySourceIdOrderByCapturedAtDesc(Long sourceId);
}
