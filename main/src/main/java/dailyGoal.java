public class dailyGoal {
    String email;
    int goalWeight;
    int steps;
    int calories;

    public dailyGoal(String email, int goalWeight, int steps, int calories) {
        this.email = email;
        this.goalWeight = goalWeight;
        this.steps = steps;
        this.calories = calories;
    }

    public String getEmail() {
        return email;
    }

    public int getGoalWeight() {
        return goalWeight;
    }

    public int getSteps() {
        return steps;
    }

    public int getCalories() {
        return calories;
    }

    public String toString(){
        String returnMe = "";
        returnMe += "Goal Weight: " +  goalWeight + " pounds. \n";
        returnMe += "Steps: " + steps + "\n";
        returnMe += "Calories: " + calories + "\n";
        return returnMe;
    }

}
