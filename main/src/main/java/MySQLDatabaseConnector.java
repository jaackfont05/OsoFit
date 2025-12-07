import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class MySQLDatabaseConnector {

    private static final String HOST = "34.69.24.249";
    private static final int PORT = 3306;

    private static final String DATABASE_NAME = "OsoFitData";
    private static final String USERNAME = "jackFontenot";
    private static final String PASSWORD = "12345";

    private static final String JDBC_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME +
            "?useSSL=true&serverTimezone=UTC";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    }

    public static void connect() {
        try (Connection connection = getConnection()) {
            if (connection != null) {
                System.out.println("Successfully connected to the MySQL database!");
            }
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public boolean insertUser(user u) throws SQLException {
        String query = "INSERT INTO users (userN, email, passW, city, animal, role) VALUES (?, ?, ?, ?, ?, ?)";
        String userN = u.getUserN();
        String email = u.getEmail();
        String password = u.getPassword();
        String city = u.getCity();
        String animal = u.getAnimal();
        String role = u.getRole();

        try (Connection connection = getConnection()) {
            if(userExists(u)){
                return false;
            }
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userN);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, city);
            preparedStatement.setString(5, animal);
            preparedStatement.setString(6, role);

            int row = preparedStatement.executeUpdate();
            return row > 0;
        }catch (SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean userExists(user u) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ? OR userN = ?";
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            preparedStatement.setString(2, u.getUserN());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public String[] loginUser(String email, String pass) throws SQLException {
        String query = "SELECT userN, email, passW, city, animal, role FROM users WHERE email = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String storedPassword = resultSet.getString("passW");
                    if (pass.equals(storedPassword)) {
                        return new String[] {
                                resultSet.getString("userN"),
                                resultSet.getString("email"),
                                storedPassword,
                                resultSet.getString("city"),
                                resultSet.getString("animal"),
                                resultSet.getString("role")
                        };
                    }
                }
            }
        }
        return null;
    }

    public boolean verifyUser(String email, String city, String anim) throws SQLException{
        String query = "SELECT * FROM users WHERE email = ? AND city = ? AND animal = ?";
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, city);
            preparedStatement.setString(3, anim);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {

        }
        return false;
    }

    public boolean updatePass(String email, String pass) throws SQLException {
        String query = "UPDATE users SET passW = ? WHERE email = ?";
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, pass);
            preparedStatement.setString(2, email);
            int rows = preparedStatement.executeUpdate();
            return rows > 0;
        }catch (SQLException e) {}

        return false;
    }

    public boolean createExercise(Exercise e, user u) throws SQLException {
        String query = "INSERT INTO Exercises (email, exerciseID, name, weight_pounds, equipment, reps, sets) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            preparedStatement.setInt(2, e.getID());
            preparedStatement.setString(3, e.name);
            preparedStatement.setInt(4, e.weight);
            preparedStatement.setString(5, e.equipment);
            preparedStatement.setInt(6, e.reps);
            preparedStatement.setInt(7, e.sets);
            System.out.println("Saving exercise: " +  e.toString());
            int row = preparedStatement.executeUpdate();
            return row > 0;
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return false;
    }

    public boolean createReminder(Reminder r, user u) throws SQLException {
        String query = "INSERT INTO reminders (email, title, description, frequency) VALUES (?, ?, ?, ?)";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            preparedStatement.setString(2, r.getTitle());
            preparedStatement.setString(3, r.getDescription());
            preparedStatement.setInt(4, r.getFrequency());
            System.out.println("Saving reminder: " +  r.toString());
            int row = preparedStatement.executeUpdate();
            return row > 0;
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return false;
    }

    public ArrayList<Reminder> getReminders(user u) throws SQLException {
        String getReminderSQL = "SELECT * from reminders where email = ?";
        ArrayList<Reminder> returnMe = new ArrayList<>();

        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(getReminderSQL);
            preparedStatement.setString(1, u.getEmail());
            System.out.println("Retreiving user's reminders");
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()) {
                String email = rs.getString("email");
                String title =  rs.getString("title");
                String description =  rs.getString("description");
                int frequency = rs.getInt("frequency");
                returnMe.add(new Reminder(email, title, description, frequency));
            }
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        return returnMe;
    }

    public boolean deleteReminder(Reminder r, user u) throws SQLException {
        String query = "DELETE from reminders where email = ? and title = ? and description = ? and frequency = ?";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            preparedStatement.setString(2, r.getTitle());
            preparedStatement.setString(3, r.getDescription());
            preparedStatement.setInt(4, r.getFrequency());
            System.out.println("Deleting reminder: " +  r.toString());
            int row = preparedStatement.executeUpdate();
            return row > 0;
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return false;
    }

    public boolean createStatistic(Statistic s, user u) throws SQLException {
        String query = "INSERT INTO Stats (email, date_time, weight_pounds, steps) VALUES (?, ?, ?, ?)";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            preparedStatement.setDate(2,s.getDate());
            preparedStatement.setLong(3, s.getWeight());
            preparedStatement.setInt(4, s.getSteps());
            System.out.println("Saving statistic: " +  s.toString());
            int row = preparedStatement.executeUpdate();
            return row > 0;
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return false;
    }

    public ArrayList<Statistic> getStatistics(user u){
        ArrayList<Statistic> returnMe = new ArrayList<>();
        String query = "SELECT * from Stats where email = ?";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()) {
                String email = rs.getString("email");
                Date dateTime = rs.getDate("date_time");
                Long weight = rs.getLong("weight_pounds");
                int steps = rs.getInt("steps");
                returnMe.add(new Statistic(email, dateTime, weight, steps));
            }
            Collections.sort(returnMe);
        }catch(SQLException ex){
            System.out.println("Error retrieving statistics");
        }

        return returnMe;
    }

    public boolean createSleep(Sleep s, user u) {
        String query = "INSERT INTO Sleep (email, hours, quality, date) VALUES (?, ?, ?, ?)";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            preparedStatement.setInt(2,s.getHours());
            preparedStatement.setInt(3, s.getQuality());
            preparedStatement.setDate(4, s.getDate());
            int row = preparedStatement.executeUpdate();
            updateHourProgress(s,u);
            return row > 0;
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        return false;
    }

    public ArrayList<Sleep> getSleepRecords(user u){
        ArrayList<Sleep> returnMe = new ArrayList<>();
        String query = "SELECT * from Sleep where email = ?";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()) {
                String email = rs.getString("email");
                int hours = rs.getInt("hours");
                int quality = rs.getInt("quality");
                Date date = rs.getDate("date");
                returnMe.add(new Sleep(email, hours, quality, date));
            }
            Collections.sort(returnMe);
        }catch(SQLException ex){
            System.out.println("Error retrieving sleep records");
        }

        return returnMe;
    }

    public boolean createSleepGoal(sleepGoal g, user u) {
        String query = "INSERT INTO SleepGoals (email, totalHours, currentHours, minQuality, startDate, endDate) VALUES (?, ?, ?, ?, ?, ?)";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            preparedStatement.setInt(2,g.getTotalHours());
            preparedStatement.setInt(3, getHourProgress(g, u));
            preparedStatement.setInt(4,g.getMinimumQuality());
            preparedStatement.setDate(5,g.getStartDate());
            preparedStatement.setDate(6,g.getEndDate());
            int row = preparedStatement.executeUpdate();
            return row > 0;
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        return false;
    }

    public int getHourProgress(sleepGoal g, user u) {
        String query = "SELECT * FROM Sleep WHERE email = ? AND date BETWEEN ? and ?";
        int count = 0;
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            preparedStatement.setDate(2,g.getStartDate());
            preparedStatement.setDate(3,g.getEndDate());
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()) {
                count += rs.getInt("hours");
            }
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        g.setHourProgress(count);
        return count;
    }

    public int updateHourProgress(Sleep s, user u) {
        ArrayList<sleepGoal> sleepGoals = getSleepGoals(u);
        int count = 0;
        int updatedHours;
        for(sleepGoal sg : sleepGoals) {
            updatedHours = getHourProgress(sg, u);
            if((s.getDate().compareTo(sg.getStartDate()) > 1) && (s.getDate().compareTo(sg.getEndDate()) < 1)) {
                if (s.getQuality() >= sg.getMinimumQuality()) {
                    updatedHours += s.getHours();
                    String query = "UPDATE sleepGoal SET currentHours = ? WHERE " +
                            "email = ? and totalHours = ? and currentHours = ? and minQuality = ? and startDate = ? and endDate = ?";
                    try(Connection connection = getConnection()) {
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setInt(1, updatedHours);
                        preparedStatement.setString(2, u.getEmail());
                        preparedStatement.setInt(3, sg.getTotalHours());
                        preparedStatement.setInt(4, sg.getMinimumQuality());
                        preparedStatement.setDate(5,sg.getStartDate());
                        preparedStatement.setDate(6,sg.getEndDate());
                        ResultSet rs = preparedStatement.executeQuery();
                        count++;
                    }catch(SQLException ex){
                        System.out.println("Error updating sleep records");
                    }
                }
            }
        }
        return count;
    }

    public ArrayList<sleepGoal> getSleepGoals(user u){
        ArrayList<sleepGoal> returnMe = new ArrayList<>();
        String query = "SELECT * from SleepGoals where email = ?";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()) {
                String email = rs.getString("email");
                int totalHours = rs.getInt("totalHours");
                int currentHours = rs.getInt("currentHours");
                int quality = rs.getInt("minQuality");
                Date startDate = rs.getDate("startDate");
                Date endDate = rs.getDate("endDate");
                returnMe.add(new sleepGoal(email, totalHours, currentHours, quality, startDate, endDate));
            }
            Collections.sort(returnMe);
        }catch(SQLException ex){
            System.out.println("Error retrieving sleep records");
        }

        return returnMe;
    }

    public boolean deleteSleepGoal(sleepGoal sg, user u) throws SQLException {
        String query = "DELETE from SleepGoals where email = ? and totalHours = ? and currentHours = ? and minQuality = ? " +
                "and startDate = ? and endDate = ?";
        try(Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, u.getEmail());
            preparedStatement.setInt(2, sg.getTotalHours());
            preparedStatement.setInt(3, sg.getCurrentHours());
            preparedStatement.setInt(4, sg.getMinimumQuality());
            preparedStatement.setDate(5,sg.getStartDate());
            preparedStatement.setDate(6,sg.getEndDate());
            System.out.println("Deleting reminder: " +  sg.toString());
            int row = preparedStatement.executeUpdate();
            System.out.println(row);
            return row > 0;
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }

        return false;
    }

}