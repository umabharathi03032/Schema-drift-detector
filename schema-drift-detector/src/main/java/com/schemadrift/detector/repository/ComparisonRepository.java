package com.schemadrift.detector.repository;

import com.schemadrift.detector.model.Comparison;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComparisonRepository extends JpaRepository<Comparison, Long> {
    List<Comparison> findBySourceIdOrderByCreatedAtDesc(Long sourceId);
}
