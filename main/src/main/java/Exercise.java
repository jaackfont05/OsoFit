import javax.swing.*;
import java.util.Objects;

public class Exercise {
    private int exerciseID;
    public String name;
    public int weight;
    public String equipment;
    public int sets;
    public int reps;

    public Exercise(int weight, String equipment, int sets, int reps, String name) {
        this.weight = weight;
        this.equipment = equipment;
        this.sets = sets;
        this.reps = reps;
        this.name = name;
        setID();
    }

    public Exercise() {}

    private void setID(){
        exerciseID = Objects.hash(name,weight,equipment,sets,reps);
    }
}
