import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

//Add menBar object for your own main page UI functionality
public class menuBar extends JPanel {
    private JFrame owner;       // which page has a menu bar
    private user currentUser;   // logged-in user
    private MySQLDatabaseConnector db;
    public menuBar(user currentUser) {
        this.currentUser = currentUser;
    }

    //!!! add your menu button variable here
    private JButton mainBtn;
    private JButton addExerciseBtn;
    private JButton setGoals;
    private JButton sleepBtn;
    private JButton workoutBtn;
    private JButton mealBtn;
    private JButton createWorkBtn;
    private JButton statsBtn;
    private JButton myClassesPlansBtn;
    private JButton reminderBtn;
    private JButton socialBtn;

    //trainer buttons
    private JButton trainerProgramsBtn;
    private JButton trainerSessionsBtn;
    private JButton previewSearchBtn;
    //After we know the owner, we can decide which page need dispose when we click
    // a button on the menu bar.
    public menuBar(JFrame owner, user currentUser, MySQLDatabaseConnector db) {
        this.owner = owner;
        this.currentUser = currentUser;
        this.db = db;

        setBackground(defaultSettings.BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1, 48));
        setBorder(new MatteBorder(0, 0, 2, 0, new Color(220, 0, 0))); // thin red line under bar

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        row.setOpaque(false);

        //role check
        boolean isTrainer = "trainer".equalsIgnoreCase(currentUser.getRole());

        //!!!declare your menu button variable by using otherFeatureButton()
        mainBtn = mainPageBtn(" Main Page ");  //
        /*addExerciseBtn = otherFeatureBtn(" Add Exercise ",
                new Color(200, 40, 40)); // you can choose different color if you want :)*/

        //HelpMe: if someone can add a short sound when user is clicking a menu button, that would be perfect!
        //main page button for every one.
        mainBtn.addActionListener(e -> {
            // If we're already on main, just bring it to front
            if (owner instanceof mainPage) {
                owner.toFront(); // ensures it appears on top just in case
                owner.requestFocus(); // keep your current input field is the same if click the btn on menu
                return;
            }
            if (owner != null) owner.dispose();           // close current page
            new mainPage(currentUser, db).setVisible(true);   // open main page
        });
        row.add(mainBtn);

        if (isTrainer) {
            // TRAINER MENU

            trainerProgramsBtn = otherFeatureBtn(" Trainer Programs ", new Color(200, 40, 40));
            trainerProgramsBtn.addActionListener(e -> {
                if (owner instanceof TrainerProgramsPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new TrainerProgramsPage(currentUser, db).setVisible(true);
            });

            trainerSessionsBtn = otherFeatureBtn(" Sessions Today ", new Color(200, 40, 40));
            trainerSessionsBtn.addActionListener(e -> {
                if (owner instanceof SessionsTodayPage) { // you'll create this JFrame later
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new SessionsTodayPage(currentUser, db).setVisible(true);
            });

            previewSearchBtn = otherFeatureBtn(" Preview Search ", new Color(200, 40, 40));
            previewSearchBtn.addActionListener(e -> {

                if (owner instanceof TrainerProgramPreviewPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }

                if (owner != null) owner.dispose();
                new TrainerProgramPreviewPage(currentUser, db).setVisible(true);
            });


            row.add(trainerProgramsBtn);
            row.add(trainerSessionsBtn);
            row.add(previewSearchBtn);

        } else {
            //USER MENU
            addExerciseBtn = otherFeatureBtn(" Add Exercise ", new Color(200, 40, 40));
            addExerciseBtn.addActionListener(e -> {
                //add your customized actionListener, don't forget
                //check whether to dispose current page/ui if needed
                if (owner instanceof exercisePage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new exercisePage(currentUser, db).setVisible(true);
            });
            //!!!add your actionListener for your own button here

            setGoals = otherFeatureBtn(" Set Goals ", new Color(200, 40, 40));
            setGoals.addActionListener(e -> {
                if (owner instanceof SetGoalsPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new SetGoalsPage(db, currentUser).setVisible(true);
            });

            sleepBtn = otherFeatureBtn(" Log Sleep & Sleep Goals ", new Color(200, 40, 40));

            sleepBtn.addActionListener(e -> {
                if (owner instanceof sleepPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();

                new sleepPage(currentUser, db).setVisible(true);
            });


            workoutBtn = otherFeatureBtn(" Log Workout ", new Color(200, 40, 40));
            workoutBtn.addActionListener(e -> {
                if (owner instanceof LogWorkoutPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new LogWorkoutPage(currentUser, db).setVisible(true);
            });

            createWorkBtn = otherFeatureBtn(" Create Workout ", new Color(200, 40, 40));
            createWorkBtn.addActionListener(e -> {
                if (owner instanceof CreateWorkoutPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new CreateWorkoutPage(currentUser, db).setVisible(true);
            });


            mealBtn = otherFeatureBtn(" Log Meal ", new Color(200, 40, 40));
            mealBtn.addActionListener(e -> {
                if (owner instanceof MealPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new MealPage(currentUser, db).setVisible(true);
            });

            statsBtn = otherFeatureBtn("  Stats  ", new Color(200, 40, 40));
            statsBtn.addActionListener(e -> {
                if (owner instanceof StatsPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new StatsPage(currentUser, db).setVisible(true);
            });

            myClassesPlansBtn = otherFeatureBtn(" My Classes & Plans ", new Color(200, 40, 40));
            myClassesPlansBtn.addActionListener(e -> {
                if (owner instanceof UserProgramsPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new UserProgramsPage(currentUser, db).setVisible(true);
            });

            reminderBtn = otherFeatureBtn(" Reminders ", new Color(200, 40, 40));
            reminderBtn.addActionListener(e -> {
                if (owner instanceof ReminderPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new ReminderPage(currentUser, db).setVisible(true);
            });

            socialBtn = otherFeatureBtn(" Social ", new Color(200, 40, 40));
            socialBtn.addActionListener(e -> {
                if (owner instanceof SocialsPage) {
                    owner.toFront();
                    owner.requestFocus();
                    return;
                }
                if (owner != null) owner.dispose();
                new SocialsPage(currentUser, db).setVisible(true);
            });

            //!!!don't forget add your btn in row here
            //row.add(mainBtn);
            row.add(addExerciseBtn);
            row.add(setGoals);
            row.add(sleepBtn);
            row.add(workoutBtn);
            row.add(mealBtn);
            row.add(createWorkBtn);
            row.add(statsBtn);
            row.add(myClassesPlansBtn);
            row.add(reminderBtn);
            row.add(socialBtn);
            //add(row, BorderLayout.CENTER);
        }
        add(row, BorderLayout.CENTER);
    }

    // Btn helpers
    private JButton mainPageBtn(String text) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);                // text color
        b.setBackground(defaultSettings.BACKGROUND_COLOR);          // button fill (black)
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(defaultSettings.BORDER_COLOR, 2, true)); // green outline
        return b;
    }

    private JButton otherFeatureBtn(String text, Color borderColor) {
        JButton b = new JButton(text);
        b.setForeground(defaultSettings.TEXT_COLOR);
        b.setBackground(defaultSettings.BACKGROUND_COLOR);
        b.setFont(defaultSettings.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(borderColor, 2, true));          // red outline
        return b;
    }


}

