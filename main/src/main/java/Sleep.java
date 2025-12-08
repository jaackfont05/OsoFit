import java.sql.Date;

public class Sleep implements Comparable<Sleep> {
    private String email;
    private int hours;
    private int quality;
    private Date date;

    public Sleep(String email, int hours, int quality, Date date) {
        this.email = email;
        this.hours = hours;
        this.quality = quality;
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public int getHours() {
        return hours;
    }

    public int getQuality() {
        return quality;
    }

    public Date getDate() {
        return date;
    }

    public String toString(){
        String returnMe = "";
        returnMe += date.toString();
        returnMe += ": Slept for ";
        returnMe += hours;
        returnMe += " hours with a quality rating of ";
        returnMe += quality;
        returnMe += ".";
        return returnMe;
    }

    public int compareTo(Sleep other){
        return (-1) * date.compareTo(other.date);
    }
}
