// Savannah Johnson
public class weightGoal {
   private double currWeight;
   private double goalWeight;

   public weightGoal(double currWeight, double goalWeight) {
       this.currWeight = currWeight;
       this.goalWeight = goalWeight;
   }

   public double getCurrWeight() {
       return currWeight;
   }

   public void setCurrWeight(double currWeight) {
       this.currWeight = currWeight;
   }

   public double getGoalWeight() {
       return goalWeight;
   }

   public void setGoalWeight(double goalWeight) {
       this.goalWeight = goalWeight;
   }
}
