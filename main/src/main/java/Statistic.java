/**
 * Created by Mason Baxter
 * The Statistic class used for logging a user's weight and steps for a particular day
 */

import java.sql.Date;

public class Statistic implements Comparable<Statistic> {
    private String email;
    private Date date;
    private long weight;
    private int steps;

    public Statistic(String email, Date date, long weight, int steps) {
        this.email = email;
        this.date = date;
        this.weight = weight;
        this.steps = steps;
    }

    public String getEmail(){
        return email;
    }

    public Date getDate(){
        return date;
    }

    public long getWeight(){
        return weight;
    }

    public int getSteps(){
        return steps;
    }

    public String toString(){
        String returnMe = "";
        returnMe += "On ";
        returnMe += date.toString();
        returnMe += ", you weighed ";
        returnMe += weight;
        returnMe += " pounds and travelled ";
        returnMe += steps;
        returnMe += " steps!";
        return returnMe;
    }

    public int compareTo(Statistic other){
        return date.compareTo(other.date);
    }
}
