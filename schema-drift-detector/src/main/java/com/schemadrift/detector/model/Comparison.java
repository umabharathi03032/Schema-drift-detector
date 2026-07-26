package com.schemadrift.detector.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comparisons")
public class Comparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_snapshot_id", nullable = false)
    private SchemaSnapshot oldSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_snapshot_id", nullable = false)
    private SchemaSnapshot newSnapshot;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity overallStatus; // BREAKING if any change is breaking, else SAFE

    @OneToMany(mappedBy = "comparison", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FieldChange> changes = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public SchemaSnapshot getOldSnapshot() { return oldSnapshot; }
    public void setOldSnapshot(SchemaSnapshot oldSnapshot) { this.oldSnapshot = oldSnapshot; }

    public SchemaSnapshot getNewSnapshot() { return newSnapshot; }
    public void setNewSnapshot(SchemaSnapshot newSnapshot) { this.newSnapshot = newSnapshot; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Severity getOverallStatus() { return overallStatus; }
    public void setOverallStatus(Severity overallStatus) { this.overallStatus = overallStatus; }

    public List<FieldChange> getChanges() { return changes; }
    public void setChanges(List<FieldChange> changes) { this.changes = changes; }
}
