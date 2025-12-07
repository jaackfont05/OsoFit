//Mason Baxter

import java.sql.Date;

public class sleepGoal implements Comparable<sleepGoal>{
    String email;
    int numTotalHours;
    int currentHours;
    int minimumQuality;
    Date startDate;
    Date endDate;

    public sleepGoal(String email, int totalHours, int currentHours, int minQuality, Date startDate, Date finalDate){
        this.email = email;
        numTotalHours = totalHours;
        currentHours = 0;
        minimumQuality = minQuality;
        this.startDate = startDate;
        endDate = finalDate;
    }

    public sleepGoal(String email, int totalHours, int minQuality, Date startDate, Date finalDate){
        this.email = email;
        numTotalHours = totalHours;
        currentHours = 0;
        minimumQuality = minQuality;
        this.startDate = startDate;
        endDate = finalDate;
    }

    public String getEmail() {
        return email;
    }

    public int getTotalHours(){return numTotalHours;}

    public int getCurrentHours(){return currentHours;}

    public int getMinimumQuality(){return minimumQuality;}

    public Date getStartDate(){return startDate;}

    public Date getEndDate(){return endDate;}

    public void setHourProgress(int hourProgress){
        currentHours = hourProgress;
    }

    public String toString(){
        String returnMe = "";
        returnMe += "Get " + numTotalHours;
        returnMe += " total hours of sleep between the dates of ";
        returnMe += startDate.toString() + " and " + endDate.toString();
        returnMe += "\n";
        returnMe += "Minimum Sleep Quality to Count towards goal: " + minimumQuality;
        returnMe += "\n";
        returnMe += "Current Progress: " +  currentHours;
        returnMe += "/" + numTotalHours;
        return returnMe;
    }

    public int compareTo(sleepGoal other){
        return (-1) * endDate.compareTo(other.endDate);
    }

}
