package com.ZeDoExter.mvc.model;

import java.time.Instant;

public class Pledge {
    private String id;
    private String userId;
    private String projectId;
    private Instant timestamp;
    private double amount;
    private String rewardTierId; // optional
    private PledgeStatus status;
    private String rejectionReason; // if REJECTED

    public Pledge() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getRewardTierId() { return rewardTierId; }
    public void setRewardTierId(String rewardTierId) { this.rewardTierId = rewardTierId; }

    public PledgeStatus getStatus() { return status; }
    public void setStatus(PledgeStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}

