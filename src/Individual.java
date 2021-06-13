/**
 * Represents an individual of population for GeneticAlgorithm
 * An individual holds the reference to the Schedule and fitness.
 */
public class Individual {
    private Schedule schedule;
    private double fitness;

    public Individual(Schedule schedule) {
        this.schedule = schedule;
    }

    public Individual(Individual another) {
        this.schedule = new Schedule(another.schedule);
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
