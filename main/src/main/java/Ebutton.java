//Exercise Button
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Ebutton extends JButton{
    public Exercise exercise;
    public Ebutton() {
        super("Add Exercise");
        this.setBounds(300,200,200,50);

        this.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                main.frame.remove(Ebutton.this);
                Epanel panel = new Epanel();
                panel.setVisible(true);
                panel.setBounds(0,0,400,400);
                exercise = panel.getExercise();
                main.frame.add(panel);
                main.frame.setVisible(true);
                main.frame.revalidate();
                main.frame.repaint();
            }
        });

    }


    public Exercise getExercise(){
        return exercise;
    }
}
