package com.schemadrift.detector.dto;

public class CaptureSnapshotRequest {
    private String jdbcUrl;
    private String username;
    private String password;
    private String tableName;

    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
}
