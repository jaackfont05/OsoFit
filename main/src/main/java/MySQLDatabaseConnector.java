import java.sql.*;

public class MySQLDatabaseConnector {

    private static final String HOST = "34.69.24.249";
    private static final int PORT = 3306;

    private static final String DATABASE_NAME = "OsoFitData";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123";

    private static final String JDBC_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME +
            "?useSSL=true&serverTimezone=UTC";

    private static Connection getConnection() throws SQLException {
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
        String query = "INSERT INTO userInfo (username, email, _password, city, animal, role) VALUES (?, ?, ?, ?, ?, ?)";
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
        String query = "SELECT * FROM userInfo WHERE email = ? OR username = ?";
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
        String query = "SELECT username, email, _password, city, animal, role FROM userInfo WHERE email = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String storedPassword = resultSet.getString("_password");
                    if (pass.equals(storedPassword)) { // Note: In production, use hashed passwords and secure comparison
                        return new String[] {
                                resultSet.getString("username"),
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
        String query = "SELECT * FROM userInfo WHERE email = ? AND city = ? AND animal = ?";
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
        String query = "UPDATE userInfo SET _password = ? WHERE email = ?";
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, pass);
            preparedStatement.setString(2, email);
            int rows = preparedStatement.executeUpdate();
            return rows > 0;
        }catch (SQLException e) {}

        return false;
    }
}