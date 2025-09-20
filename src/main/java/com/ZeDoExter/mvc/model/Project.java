package com.ZeDoExter.mvc.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private String id; // 8 digits, first not 0
    private String name;
    private String category;
    private double goalAmount;
    private LocalDate deadline;
    private LocalDate createdAt;
    private double currentRaised;
    private boolean hasStretchGoals;
    private int rejectedPledges;

    private List<RewardTier> rewardTiers = new ArrayList<>();
    private List<StretchGoal> stretchGoals = new ArrayList<>();

    public Project() {}

    public Project(String id, String name, String category, double goalAmount, LocalDate deadline, boolean hasStretchGoals) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.goalAmount = goalAmount;
        this.deadline = deadline;
        this.hasStretchGoals = hasStretchGoals;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getGoalAmount() { return goalAmount; }
    public void setGoalAmount(double goalAmount) { this.goalAmount = goalAmount; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public double getCurrentRaised() { return currentRaised; }
    public void setCurrentRaised(double currentRaised) { this.currentRaised = currentRaised; }

    public boolean isHasStretchGoals() { return hasStretchGoals; }
    public void setHasStretchGoals(boolean hasStretchGoals) { this.hasStretchGoals = hasStretchGoals; }

    public int getRejectedPledges() { return rejectedPledges; }
    public void setRejectedPledges(int rejectedPledges) { this.rejectedPledges = rejectedPledges; }

    public List<RewardTier> getRewardTiers() { return rewardTiers; }
    public void setRewardTiers(List<RewardTier> rewardTiers) { this.rewardTiers = rewardTiers; }

    public List<StretchGoal> getStretchGoals() { return stretchGoals; }
    public void setStretchGoals(List<StretchGoal> stretchGoals) { this.stretchGoals = stretchGoals; }
}
