import java.time.LocalDateTime;

/**
 * Connor Griffin - Workout entity class
 * Represents one logged workout session.
 */
public class workout {
    private int workoutId;
    private String userId;
    private String type;
    private int durationMin;
    private int calories;
    private String notes;
    private LocalDateTime dateTime;

    public Workout(int workoutId, String userId, String type, int durationMin, int calories, String notes, LocalDateTime dateTime) {
        this.workoutId = workoutId;
        this.userId = userId;
        this.type = type;
        this.durationMin = durationMin;
        this.calories = calories;
        this.notes = notes;
        this.dateTime = dateTime;
    }

    // Getters & Setters
    public int getWorkoutId() { return workoutId; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public int getDurationMin() { return durationMin; }
    public int getCalories() { return calories; }
    public String getNotes() { return notes; }
    public LocalDateTime getDateTime() { return dateTime; }

    public void setType(String type) { this.type = type; }
    public void setDurationMin(int durationMin) { this.durationMin = durationMin; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %d min, %d cal (%s)", 
            dateTime.toLocalDate(), type, durationMin, calories, notes == null ? "" : notes);
    }
}
