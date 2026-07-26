package com.schemadrift.detector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "field_changes")
public class FieldChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JsonIgnore breaks the Comparison <-> FieldChange serialization cycle
    // (comparison.getChanges() -> change.getComparison() -> ... forever).
    // The comparison is always accessed from the Comparison side, so the
    // frontend never needs this back-reference in the payload.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comparison_id", nullable = false)
    private Comparison comparison;

    @Column(nullable = false)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeType changeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column
    private String oldValue; // e.g. "INT, nullable" - human readable snapshot of old state

    @Column
    private String newValue;

    @Lob
    @Column
    private String aiExplanation; // filled in later by the AI explanation layer

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Comparison getComparison() { return comparison; }
    public void setComparison(Comparison comparison) { this.comparison = comparison; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public ChangeType getChangeType() { return changeType; }
    public void setChangeType(ChangeType changeType) { this.changeType = changeType; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getAiExplanation() { return aiExplanation; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }
}
