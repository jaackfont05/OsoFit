import java.time.LocalDateTime;
import java.util.*;

/**
 * Connor Griffin - Workout entity class
 * Represents one logged workout session.
 */
public class Workout {
    private int workoutId;
    private String email;
    private String type;
    private int durationMin;
    private int calories;
    private LocalDateTime dateTime;
    public List<Exercise> exercises;

    public Workout(int workoutId, String userId, String type, int durationMin, int calories, LocalDateTime dateTime, List<Exercise> exercises){
        this.workoutId = workoutId;
        this.email = userId;
        this.type = type;
        this.durationMin = durationMin;
        this.calories = calories;
        this.dateTime = dateTime;
        this.exercises = exercises;
    }

    // Getters & Setters
    public int getWorkoutId() { return workoutId; }
    public String getEmail() { return email; }
    public String getType() { return type; }
    public int getDurationMin() { return durationMin; }
    public int getCalories() { return calories; }
    public LocalDateTime getDateTime() { return dateTime; }

    public void setType(String type) { this.type = type; }
    public void setDurationMin(int durationMin) { this.durationMin = durationMin; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

}
