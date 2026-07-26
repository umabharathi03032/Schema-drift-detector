package com.schemadrift.detector.repository;

import com.schemadrift.detector.model.SchemaField;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SchemaFieldRepository extends JpaRepository<SchemaField, Long> {
    List<SchemaField> findBySnapshotId(Long snapshotId);
}
