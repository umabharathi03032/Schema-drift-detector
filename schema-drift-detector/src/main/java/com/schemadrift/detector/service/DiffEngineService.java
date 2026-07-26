package com.schemadrift.detector.service;

import com.schemadrift.detector.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core differentiator of the project: compares two schema snapshots field by
 * field and classifies each difference as SAFE or BREAKING.
 *
 * The rules here are intentionally explicit and readable rather than clever -
 * in an interview you want to be able to point at a rule and explain exactly
 * why it exists.
 */
@Service
public class DiffEngineService {

    // Type widening table: key can safely become any type in its value set
    // without breaking existing consumers (numeric range grows, no data loss).
    private static final Map<String, Set<String>> SAFE_TYPE_WIDENING = Map.of(
            "TINYINT", Set.of("SMALLINT", "MEDIUMINT", "INT", "BIGINT"),
            "SMALLINT", Set.of("MEDIUMINT", "INT", "BIGINT"),
            "MEDIUMINT", Set.of("INT", "BIGINT"),
            "INT", Set.of("BIGINT"),
            "FLOAT", Set.of("DOUBLE")
    );

    public Comparison compare(Source source, SchemaSnapshot oldSnapshot, SchemaSnapshot newSnapshot) {
        Map<String, SchemaField> oldFields = index(oldSnapshot.getFields());
        Map<String, SchemaField> newFields = index(newSnapshot.getFields());

        List<FieldChange> changes = new ArrayList<>();

        // Fields present in old but missing in new -> REMOVED
        for (String fieldName : oldFields.keySet()) {
            if (!newFields.containsKey(fieldName)) {
                changes.add(buildChange(fieldName, ChangeType.REMOVED, Severity.BREAKING,
                        describe(oldFields.get(fieldName)), null,
                        "Field was removed. Any code reading this field will fail or silently return null."));
            }
        }

        // Fields present in new but not in old -> ADDED
        for (String fieldName : newFields.keySet()) {
            if (!oldFields.containsKey(fieldName)) {
                SchemaField field = newFields.get(fieldName);
                boolean requiredWithNoDefault = !field.isNullable() && isBlank(field.getDefaultValue());
                Severity severity = requiredWithNoDefault ? Severity.BREAKING : Severity.SAFE;
                String reason = requiredWithNoDefault
                        ? "Field was added as required with no default. Existing insert statements that don't supply it will fail."
                        : "Field was added as optional. Existing code is unaffected.";
                changes.add(buildChange(fieldName, ChangeType.ADDED, severity,
                        null, describe(field), reason));
            }
        }

        // Fields present in both -> check type and nullability changes
        for (String fieldName : oldFields.keySet()) {
            if (!newFields.containsKey(fieldName)) continue;
            SchemaField oldField = oldFields.get(fieldName);
            SchemaField newField = newFields.get(fieldName);

            if (!oldField.getDataType().equalsIgnoreCase(newField.getDataType())) {
                Severity severity = isSafeWidening(oldField.getDataType(), newField.getDataType())
                        ? Severity.SAFE : Severity.BREAKING;
                String reason = severity == Severity.SAFE
                        ? "Type was widened in a backward-compatible way; existing values still fit."
                        : "Type changed incompatibly; existing consumers expecting the old type may fail or misread data.";
                changes.add(buildChange(fieldName, ChangeType.TYPE_CHANGED, severity,
                        describe(oldField), describe(newField), reason));
            }

            if (oldField.isNullable() && !newField.isNullable()) {
                changes.add(buildChange(fieldName, ChangeType.NULLABILITY_CHANGED, Severity.BREAKING,
                        describe(oldField), describe(newField),
                        "Field changed from nullable to required. Existing rows with null values, or code not supplying this field, will fail."));
            } else if (!oldField.isNullable() && newField.isNullable()) {
                changes.add(buildChange(fieldName, ChangeType.NULLABILITY_CHANGED, Severity.SAFE,
                        describe(oldField), describe(newField),
                        "Field changed from required to nullable. This relaxes the constraint and is backward compatible."));
            }
        }

        Comparison comparison = new Comparison();
        comparison.setSource(source);
        comparison.setOldSnapshot(oldSnapshot);
        comparison.setNewSnapshot(newSnapshot);
        boolean anyBreaking = changes.stream().anyMatch(c -> c.getSeverity() == Severity.BREAKING);
        comparison.setOverallStatus(anyBreaking ? Severity.BREAKING : Severity.SAFE);

        for (FieldChange change : changes) {
            change.setComparison(comparison);
        }
        comparison.setChanges(changes);

        return comparison;
    }

    private boolean isSafeWidening(String oldType, String newType) {
        String oldBase = baseType(oldType);
        String newBase = baseType(newType);
        return SAFE_TYPE_WIDENING.getOrDefault(oldBase, Set.of()).contains(newBase);
    }

    // Strips length/precision info, e.g. "VARCHAR(50)" -> "VARCHAR"
    private String baseType(String type) {
        int paren = type.indexOf('(');
        return (paren > 0 ? type.substring(0, paren) : type).toUpperCase();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String describe(SchemaField field) {
        if (field == null) return null;
        return String.format("%s, %s%s", field.getDataType(),
                field.isNullable() ? "nullable" : "not null",
                field.isPrimaryKey() ? ", primary key" : "");
    }

    private FieldChange buildChange(String fieldName, ChangeType type, Severity severity,
                                     String oldValue, String newValue, String reason) {
        FieldChange change = new FieldChange();
        change.setFieldName(fieldName);
        change.setChangeType(type);
        change.setSeverity(severity);
        change.setOldValue(oldValue);
        change.setNewValue(newValue);
        // Rule-based reason goes here initially; the AI explanation layer can
        // later overwrite/augment this with a richer, plain-English version.
        change.setAiExplanation(reason);
        return change;
    }

    private Map<String, SchemaField> index(List<SchemaField> fields) {
        Map<String, SchemaField> map = new LinkedHashMap<>();
        for (SchemaField f : fields) {
            map.put(f.getFieldName(), f);
        }
        return map;
    }
}
