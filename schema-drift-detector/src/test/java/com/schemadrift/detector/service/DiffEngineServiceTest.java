package com.schemadrift.detector.service;

import com.schemadrift.detector.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiffEngineServiceTest {

    private final DiffEngineService engine = new DiffEngineService();

    private SchemaField field(String name, String type, boolean nullable, String defaultVal) {
        SchemaField f = new SchemaField();
        f.setFieldName(name);
        f.setDataType(type);
        f.setNullable(nullable);
        f.setDefaultValue(defaultVal);
        return f;
    }

    @Test
    void removedField_isBreaking() {
        SchemaSnapshot oldSnap = new SchemaSnapshot();
        oldSnap.setFields(List.of(field("phone", "VARCHAR(20)", true, null)));

        SchemaSnapshot newSnap = new SchemaSnapshot();
        newSnap.setFields(List.of());

        Comparison result = engine.compare(new Source(), oldSnap, newSnap);

        assertEquals(1, result.getChanges().size());
        assertEquals(Severity.BREAKING, result.getChanges().get(0).getSeverity());
        assertEquals(ChangeType.REMOVED, result.getChanges().get(0).getChangeType());
        assertEquals(Severity.BREAKING, result.getOverallStatus());
    }

    @Test
    void addedOptionalField_isSafe() {
        SchemaSnapshot oldSnap = new SchemaSnapshot();
        oldSnap.setFields(List.of());

        SchemaSnapshot newSnap = new SchemaSnapshot();
        newSnap.setFields(List.of(field("country", "VARCHAR(50)", true, null)));

        Comparison result = engine.compare(new Source(), oldSnap, newSnap);

        assertEquals(Severity.SAFE, result.getChanges().get(0).getSeverity());
        assertEquals(Severity.SAFE, result.getOverallStatus());
    }

    @Test
    void addedRequiredFieldWithNoDefault_isBreaking() {
        SchemaSnapshot oldSnap = new SchemaSnapshot();
        oldSnap.setFields(List.of());

        SchemaSnapshot newSnap = new SchemaSnapshot();
        newSnap.setFields(List.of(field("country", "VARCHAR(50)", false, null)));

        Comparison result = engine.compare(new Source(), oldSnap, newSnap);

        assertEquals(Severity.BREAKING, result.getChanges().get(0).getSeverity());
    }

    @Test
    void intToBigint_isSafeWidening() {
        SchemaSnapshot oldSnap = new SchemaSnapshot();
        oldSnap.setFields(List.of(field("id", "INT", false, null)));

        SchemaSnapshot newSnap = new SchemaSnapshot();
        newSnap.setFields(List.of(field("id", "BIGINT", false, null)));

        Comparison result = engine.compare(new Source(), oldSnap, newSnap);

        assertEquals(Severity.SAFE, result.getChanges().get(0).getSeverity());
    }

    @Test
    void varcharShrink_isBreaking() {
        SchemaSnapshot oldSnap = new SchemaSnapshot();
        oldSnap.setFields(List.of(field("name", "VARCHAR(100)", false, null)));

        SchemaSnapshot newSnap = new SchemaSnapshot();
        newSnap.setFields(List.of(field("name", "TEXT", false, null)));

        Comparison result = engine.compare(new Source(), oldSnap, newSnap);

        assertEquals(Severity.BREAKING, result.getChanges().get(0).getSeverity());
    }

    @Test
    void nullableToNotNull_isBreaking() {
        SchemaSnapshot oldSnap = new SchemaSnapshot();
        oldSnap.setFields(List.of(field("email", "VARCHAR(100)", true, null)));

        SchemaSnapshot newSnap = new SchemaSnapshot();
        newSnap.setFields(List.of(field("email", "VARCHAR(100)", false, null)));

        Comparison result = engine.compare(new Source(), oldSnap, newSnap);

        assertEquals(Severity.BREAKING, result.getChanges().get(0).getSeverity());
        assertEquals(ChangeType.NULLABILITY_CHANGED, result.getChanges().get(0).getChangeType());
    }

    @Test
    void noChanges_producesSafeOverallStatus() {
        SchemaSnapshot oldSnap = new SchemaSnapshot();
        oldSnap.setFields(List.of(field("id", "INT", false, null)));

        SchemaSnapshot newSnap = new SchemaSnapshot();
        newSnap.setFields(List.of(field("id", "INT", false, null)));

        Comparison result = engine.compare(new Source(), oldSnap, newSnap);

        assertTrue(result.getChanges().isEmpty());
        assertEquals(Severity.SAFE, result.getOverallStatus());
    }
}
