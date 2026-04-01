public class user {
    private String userN;
    private String email;
    private String password;
    private String city;
    private String animal;
    private String role;  // "User" or "Trainer"

    public user(String username, String email, String password, String city, String animal, String role) {
        this.userN = username;
        this.email = email;
        this.password = password;
        this.city = city;
        this.animal = animal;
        this.role = role;
    }

    user(){
        this.userN = "";
        this.password = "";
        this.email = "";
        this.city = "";
        this.animal = "";
        this.role = "";
    }

    public String getUserN() {
        return userN;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
    public String getCity() {
        return city;
    }
    public String getAnimal() {
        return animal;
    }
    public String getRole() {
        return role;
    }
    // getters
    public void setUserN(String username) {
        this.userN = username;
    }

    public void setPassword(String password) { 
        this.password = password; 
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setAnimal(String animal) {
        this.animal = animal;
    }
    public void setRole(String role) {
        this.role = role;
    }


    // below methods used while user is creating account, and
    // may input special char, like "\n", ","(used for data structure in csv), and etc
    public String toCsv() {
        return csv(userN) + "," +
                csv(email)  + "," +
                csv(password) + "," +
                csv(city) + "," +
                csv(animal) + "," +
                csv(role);
    }

    private static String csv(String s) {
        if (s == null) return "";
        return s.replace(",", " ").replace("\n", " ").replace("\r", " ").trim();
    }


    @Override
    public String toString() {

        return userN + "," + email + "," + password + "," + city + "," + animal + "," + role;
    }
}

