//Mason Baxter

import java.sql.Date;

public class sleepGoal {
    int numTotalHours;
    int currentHours;
    int minimumQuality;
    Date endDate;

    public sleepGoal(int totalHours, int minQuality, Date finalDate){
        numTotalHours = totalHours;
        currentHours = 0;
        minimumQuality = minQuality;
        endDate = finalDate;
    }

    public sleepGoal(int totalHours, Date finalDate){
        numTotalHours = totalHours;
        currentHours = 0;
        minimumQuality = 1;
        endDate = finalDate;
    }

    public int getGoalTotal(){return numTotalHours;}

}
