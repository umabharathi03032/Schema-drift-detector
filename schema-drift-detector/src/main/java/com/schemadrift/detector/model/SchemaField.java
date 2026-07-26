package com.schemadrift.detector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "schema_fields")
public class SchemaField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JsonIgnore breaks the SchemaSnapshot <-> SchemaField serialization
    // cycle (snapshot.getFields() -> field.getSnapshot() -> ... forever).
    // The snapshot is always accessed from the SchemaSnapshot side, so the
    // frontend never needs this back-reference in the payload.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private SchemaSnapshot snapshot;

    @Column(nullable = false)
    private String fieldName;

    @Column(nullable = false)
    private String dataType;

    @Column(nullable = false)
    private boolean nullable;

    @Column(nullable = false)
    private boolean primaryKey;

    @Column
    private String defaultValue; // null if no default

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SchemaSnapshot getSnapshot() { return snapshot; }
    public void setSnapshot(SchemaSnapshot snapshot) { this.snapshot = snapshot; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }

    public boolean isPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
}
