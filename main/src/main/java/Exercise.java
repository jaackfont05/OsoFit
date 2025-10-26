public class Exercise {
    private int exerciseId;
    String name;
    int weight;
    String[] equipment;
    int sets;
    int reps;

    public Exercise(int weight, String[] equipment, int sets, int reps, String name) {
        this.weight = weight;
        this.equipment = equipment;
        this.sets = sets;
        this.reps = reps;
        this.name = name;
    }
}
