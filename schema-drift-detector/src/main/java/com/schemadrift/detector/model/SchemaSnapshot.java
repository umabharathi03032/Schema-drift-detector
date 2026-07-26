package com.schemadrift.detector.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schema_snapshots")
public class SchemaSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Column(nullable = false, updatable = false)
    private Instant capturedAt = Instant.now();

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchemaField> fields = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }

    public List<SchemaField> getFields() { return fields; }
    public void setFields(List<SchemaField> fields) { this.fields = fields; }
}
