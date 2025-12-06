public class Reminder {
    private String email;
    private String title;
    private String description;
    private int frequency;

    Reminder(String email, String title, String description, int frequency) {
        this.email = email;
        this.title = title;
        this.description = description;
        this.frequency = frequency;
    }

    Reminder(String email, String title, int frequency) {
        this.email = email;
        this.title = title;
        this.description = "";
        this.frequency = frequency;
    }

    public String getEmail() {
        return email;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getFrequency() {
        return frequency;
    }

    public String toString(){
        String returnMe = "";
        returnMe += title;
        returnMe += '\n';
        if(!description.isEmpty()){
            returnMe += description;
            returnMe += '\n';
        }
        returnMe += "You should do this " + frequency + " times a day.";
        return returnMe;
    }
}
