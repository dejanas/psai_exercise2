/**
 * Represents an individual of population for GeneticAlgorithm
 * An individual holds the reference to the Schedule and duration and number of conflicts
 * which are used for calculating fitness.
 */
public class Individual {
    private Schedule schedule;
    private double duration;
    private int conflicts;

    public Individual(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getConflicts() {
        return conflicts;
    }

    public void setConflicts(int conflicts) {
        this.conflicts = conflicts;
    }
}
