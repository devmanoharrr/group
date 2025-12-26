package kf7014.rewards.model;

public class RewardSummary {
    private String citizenId;
    private int totalPoints;
    private String badge; // Bronze (>=100), Silver (>=200), Gold (>=500)

    public RewardSummary() {}

    public RewardSummary(String citizenId, int totalPoints, String badge) {
        this.citizenId = citizenId;
        this.totalPoints = totalPoints;
        this.badge = badge;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }
}


