package com.ZeDoExter.mvc.model;

public class RewardTier {
    private String id;
    private String name;
    private double minAmount;
    private int totalQuota;
    private int remainingQuota;

    public RewardTier() {}

    public RewardTier(String id, String name, double minAmount, int totalQuota, int remainingQuota) {
        this.id = id;
        this.name = name;
        this.minAmount = minAmount;
        this.totalQuota = totalQuota;
        this.remainingQuota = remainingQuota;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getMinAmount() { return minAmount; }
    public void setMinAmount(double minAmount) { this.minAmount = minAmount; }

    public int getTotalQuota() { return totalQuota; }
    public void setTotalQuota(int totalQuota) { this.totalQuota = totalQuota; }

    public int getRemainingQuota() { return remainingQuota; }
    public void setRemainingQuota(int remainingQuota) { this.remainingQuota = remainingQuota; }
}

