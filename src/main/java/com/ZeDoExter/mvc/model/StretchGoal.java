package com.ZeDoExter.mvc.model;

public class StretchGoal {
    private String id;
    private double thresholdAmount; // absolute amount to unlock
    private String description;

    public StretchGoal() {}

    public StretchGoal(String id, double thresholdAmount, String description) {
        this.id = id;
        this.thresholdAmount = thresholdAmount;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getThresholdAmount() { return thresholdAmount; }
    public void setThresholdAmount(double thresholdAmount) { this.thresholdAmount = thresholdAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

