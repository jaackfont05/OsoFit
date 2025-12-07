//Mason Baxter

import java.sql.Date;

public class sleepGoal {
    String email;
    int numTotalHours;
    int currentHours;
    int minimumQuality;
    Date startDate;
    Date endDate;

    public sleepGoal(String email, int totalHours, int minQuality, Date startDate, Date finalDate){
        this.email = email;
        numTotalHours = totalHours;
        currentHours = 0;
        minimumQuality = minQuality;
        this.startDate = startDate;
        endDate = finalDate;
    }

    public sleepGoal(String email, int totalHours, Date startDate, Date finalDate){
        this.email = email;
        numTotalHours = totalHours;
        currentHours = 0;
        minimumQuality = 1;
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



}
