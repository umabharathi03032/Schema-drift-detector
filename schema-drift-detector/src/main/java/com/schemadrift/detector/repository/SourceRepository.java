package com.schemadrift.detector.repository;

import com.schemadrift.detector.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SourceRepository extends JpaRepository<Source, Long> {
    List<Source> findByOwnerId(Long ownerId);
}
