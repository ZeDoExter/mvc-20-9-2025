package com.ZeDoExter.mvc.model;

public class BasicFundingModel {
    private String projectId;
    private double goalAmount;
    private double currentRaised;

    public BasicFundingModel() {}

    public BasicFundingModel(String projectId, double goalAmount, double currentRaised) {
        this.projectId = projectId;
        this.goalAmount = goalAmount;
        this.currentRaised = currentRaised;
    }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public double getGoalAmount() { return goalAmount; }
    public void setGoalAmount(double goalAmount) { this.goalAmount = goalAmount; }

    public double getCurrentRaised() { return currentRaised; }
    public void setCurrentRaised(double currentRaised) { this.currentRaised = currentRaised; }

    public boolean isFunded() {
        return currentRaised >= goalAmount;
    }
}

