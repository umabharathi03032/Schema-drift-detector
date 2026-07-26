package com.schemadrift.detector.repository;

import com.schemadrift.detector.model.FieldChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FieldChangeRepository extends JpaRepository<FieldChange, Long> {
    List<FieldChange> findByComparisonId(Long comparisonId);
}
