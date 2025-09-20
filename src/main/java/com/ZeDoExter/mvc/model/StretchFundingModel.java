package com.ZeDoExter.mvc.model;

import java.util.ArrayList;
import java.util.List;

public class StretchFundingModel {
    private String projectId;
    private double goalAmount;
    private double currentRaised;
    private List<String> unlockedGoalIds = new ArrayList<>();

    public StretchFundingModel() {}

    public StretchFundingModel(String projectId, double goalAmount, double currentRaised) {
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

    public List<String> getUnlockedGoalIds() { return unlockedGoalIds; }
    public void setUnlockedGoalIds(List<String> unlockedGoalIds) { this.unlockedGoalIds = unlockedGoalIds; }
}

