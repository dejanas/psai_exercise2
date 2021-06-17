/**
 * Utility class for evaluation methods of the Schedule
 */
public class Evaluation {

    private final Schedule schedule;

    public Evaluation(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * Returns total duration of the project, which is the latest finish
     * time of all resources.
     */
    public int getDuration() {
        int result = 0;
        Resource[] resources = schedule.getResources();
        for (Resource r : resources) {
            if (r.getFinish() > result) {
                result = r.getFinish();
            }
        }
        return result;
    }

    /**
     * Returns sum of duration of all activities of the schedule.
     */
    public int getMaxDuration() {
        int duration = 0;
        for (Activity t : schedule.getActivities()) {
            duration += t.getDuration();
        }
        return duration;
    }

    /**
     * Returns normalized duration which is obtained by dividing duration by max duration.
     */
    public double getDurationNormalized() {
        return (double) getDuration() / (double) getMaxDuration();
    }

    public int getNumberOfConflicts(){
       ConstraintValidation validation = new ConstraintValidation(schedule);
       return validation.getNumberOfConflicts();
    }
}
