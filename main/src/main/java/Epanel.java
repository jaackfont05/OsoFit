//Exercise Panel
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Epanel extends JPanel{
    Exercise exercise;

    public Epanel(){
        super();
        //this.setBounds(0,0,400,400);
        this.setLayout(new FlowLayout());
        JTextField weightF = new JTextField("weight: ", 10);
        JTextField equipmentF = new JTextField("equipment: ", 35);
        JTextField setsF = new JTextField("sets: ", 10);
        JTextField repsF = new JTextField("reps: ", 10);
        JTextField nameF = new JTextField("name: ", 20);

        this.add(nameF);
        this.add(equipmentF);
        this.add(weightF);
        this.add(setsF);
        this.add(repsF);

        exercise = new Exercise();

        JButton button = new JButton("Add");
        this.add(button);
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                exercise.name = nameF.getText();
                exercise.equipment = equipmentF.getText();
                exercise.weight = Integer.parseInt(weightF.getText());
                exercise.reps = Integer.parseInt(repsF.getText());
                exercise.sets = Integer.parseInt(setsF.getText());
            }
        });
    }

    public Exercise getExercise(){
        return exercise;
    }
}
