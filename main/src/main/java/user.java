public class user {
    private String userN;
    private String password;

    user(String userN, String password) {
        this.userN = userN;
        this.password = password;
    }

    user(){
        this.userN = "";
        this.password = "";
    }
    public String getUserN() {
        return userN;
    }
    public String getPassword() {
        return password;
    }
}
