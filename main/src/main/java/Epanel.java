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
        JTextField weightF = new JTextField("Weight: ", 10);
        JTextField equipmentF = new JTextField("Equipment: ", 35);
        JTextField setsF = new JTextField("Sets: ", 10);
        JTextField repsF = new JTextField("Reps: ", 10);
        JTextField nameF = new JTextField("Name: ", 20);

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
                StringBuilder sb = new StringBuilder();
                sb.append(weightF.getText());
                sb.delete(0, 8);
                exercise.weight = Integer.parseInt(sb.toString());
                sb = new StringBuilder();
                sb.append(repsF.getText());
                sb.delete(0, 6);
                exercise.reps = Integer.parseInt(sb.toString());
                sb = new StringBuilder();
                sb.append(setsF.getText());
                sb.delete(0, 6);
                exercise.sets = Integer.parseInt(sb.toString());

                String s = exercise.toString();
                System.out.println(s);
            }
        });
    }

    public Exercise getExercise(){
        return exercise;
    }
}
