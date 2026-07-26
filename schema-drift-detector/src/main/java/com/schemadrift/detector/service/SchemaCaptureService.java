package com.schemadrift.detector.service;

import com.schemadrift.detector.model.SchemaField;
import com.schemadrift.detector.model.SchemaSnapshot;
import com.schemadrift.detector.model.Source;
import com.schemadrift.detector.repository.SchemaSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Connects to a target MySQL database and reads INFORMATION_SCHEMA to build a
 * SchemaSnapshot for one table. Kept deliberately simple for v1: point it at
 * a single table name and it captures column-level structure.
 */
@Service
public class SchemaCaptureService {

    private final SchemaSnapshotRepository snapshotRepository;

    @Autowired
    public SchemaCaptureService(SchemaSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * Captures the current structure of a table from an external MySQL
     * connection (the "source" being monitored - not this app's own DB).
     *
     * @param source       the Source entity this snapshot belongs to
     * @param jdbcUrl      e.g. jdbc:mysql://host:3306/target_db
     * @param username     credentials for the target DB (read-only recommended)
     * @param password
     * @param tableName    table to snapshot
     */
    public SchemaSnapshot captureFromMySql(Source source, String jdbcUrl, String username,
                                            String password, String tableName) throws Exception {
        List<SchemaField> fields = new ArrayList<>();

        String query = """
                SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;

        try (var connection = DriverManager.getConnection(jdbcUrl, username, password);
             var statement = connection.prepareStatement(query)) {

            statement.setString(1, tableName);
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SchemaField field = new SchemaField();
                    field.setFieldName(resultSet.getString("COLUMN_NAME"));
                    field.setDataType(resultSet.getString("DATA_TYPE").toUpperCase());
                    field.setNullable("YES".equalsIgnoreCase(resultSet.getString("IS_NULLABLE")));
                    field.setPrimaryKey("PRI".equalsIgnoreCase(resultSet.getString("COLUMN_KEY")));
                    field.setDefaultValue(resultSet.getString("COLUMN_DEFAULT"));
                    fields.add(field);
                }
            }
        }

        SchemaSnapshot snapshot = new SchemaSnapshot();
        snapshot.setSource(source);
        for (SchemaField field : fields) {
            field.setSnapshot(snapshot);
        }
        snapshot.setFields(fields);

        return snapshotRepository.save(snapshot);
    }
}
