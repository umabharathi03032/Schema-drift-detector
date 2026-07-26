package com.schemadrift.detector.dto;

public class CompareRequest {
    private Long sourceId;
    private Long oldSnapshotId;
    private Long newSnapshotId;

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public Long getOldSnapshotId() { return oldSnapshotId; }
    public void setOldSnapshotId(Long oldSnapshotId) { this.oldSnapshotId = oldSnapshotId; }

    public Long getNewSnapshotId() { return newSnapshotId; }
    public void setNewSnapshotId(Long newSnapshotId) { this.newSnapshotId = newSnapshotId; }
}
