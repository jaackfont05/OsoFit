public class trainer extends user{
    //other attributes may need for trainer in future
    public trainer(String username, String email, String password, String city, String animal){
        super(username, email, password, city, animal, "Trainer");
    }

    public trainer(){
        super();
    }
}
