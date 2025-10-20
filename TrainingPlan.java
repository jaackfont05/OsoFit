package osofit.workouts;

/**
 * Connor Griffin - Represents a training plan a user can join.
 */
public class TrainingPlan {
    private int planId;
    private String name;
    private String difficulty;
    private boolean active;

    public TrainingPlan(int planId, String name, String difficulty, boolean active) {
        this.planId = planId;
        this.name = name;
        this.difficulty = difficulty;
        this.active = active;
    }

    public int getPlanId() { return planId; }
    public String getName() { return name; }
    public String getDifficulty() { return difficulty; }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return name + " (" + difficulty + ") - " + (active ? "Active" : "Pending");
    }
}
