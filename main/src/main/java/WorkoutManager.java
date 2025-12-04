import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Connor Griffin - Handles workout creation, edit, and deletion.
 * Corresponds to use cases:
 * UC1 Log Workout Session
 * UC3 Edit/Delete Workout Entry
 */
public class WorkoutManager {
    private List<Workout> workouts = new ArrayList<>();
    private int nextId = 1;

    /** UC1: Log Workout Session */
    public Workout createWorkout(String userId, String type, int durationMin, int calories, String notes) {
        if (durationMin < 0 || calories < 0 || type == null || type.isBlank())
            throw new IllegalArgumentException("Invalid workout input");

        Workout w = new Workout(nextId++, userId, type, durationMin, calories, LocalDateTime.now(), null);
        workouts.add(w);
        recalculateDashboard(userId);
        System.out.println("Workout logged successfully: " + w);
        return w;
    }

    /** UC3: Edit or Delete Workout Entry */
    public void editOrDeleteWorkoutEntry(int workoutId, String action, String newType, int newDuration, int newCalories) {
        Workout target = workouts.stream().filter(w -> w.getWorkoutId() == workoutId).findFirst().orElse(null);
        if (target == null) {
            System.out.println("Workout not found.");
            return;
        }

        if (action.equalsIgnoreCase("edit")) {
            target.setType(newType);
            target.setDurationMin(newDuration);
            target.setCalories(newCalories);
            System.out.println("Workout updated: " + target);
        } else if (action.equalsIgnoreCase("delete")) {
            workouts.remove(target);
            System.out.println("Workout deleted: " + workoutId);
        }

        recalculateDashboard(target.getEmail());
    }

    /** UC3 Postcondition: update aggregates after change */
    public void recalculateDashboard(String userId) {
        int totalMinutes = workouts.stream()
                .filter(w -> w.getEmail().equals(userId))
                .mapToInt(Workout::getDurationMin)
                .sum();
        int totalCalories = workouts.stream()
                .filter(w -> w.getEmail().equals(userId))
                .mapToInt(Workout::getCalories)
                .sum();
        System.out.println("Dashboard recalculated for " + userId + 
            ": " + totalMinutes + " total minutes, " + totalCalories + " total calories");
    }

    public List<Workout> getWorkouts() {
        return workouts;
    }
}
