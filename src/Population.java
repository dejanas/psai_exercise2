import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Represents array of individuals by providing their results with the help of Evaluation class.
 */
public class Population {
    private ArrayList<Individual> individuals;
    private int id;
    private double bestTime;
    private double worstTime;
    private double sumTime;
    private double avgTime;
    private double makespan;

    Population(ArrayList<Individual> individuals, int id) {
        this.individuals = individuals;
        this.id = id;
    }

    Population(int id) {
        this.individuals = new ArrayList<>();
        this.id = id;
    }

    void addNewIndividual(Individual individual) {
        individuals.add(individual);
    }

    public String toString() {
        return id + ";" + bestTime + ";" + worstTime + ";" + avgTime + ";" + makespan + '\n';
    }

    void evaluateDuration() {
        double bestTime = 0;
        double worstTime = 0;
        double sumTime = 0;
        double makespan = 1;

        for (Individual individual : this.individuals) {
            Evaluation evaluator = new Evaluation(individual.getSchedule());
            double duration = evaluator.getDuration();
            individual.setFitness(duration);
            sumTime += duration;
            if (duration < bestTime || 0 == bestTime) {
                bestTime = duration;
            }
            if (duration > worstTime || 0 == worstTime) {
                worstTime = duration;
            }
            double durationNormalized = evaluator.getDurationNormalized();
            if(durationNormalized < makespan){
                makespan = durationNormalized;
            }
        }
        this.bestTime = roundTwoDecimals(bestTime);
        this.worstTime = roundTwoDecimals(worstTime);
        this.sumTime = roundTwoDecimals(sumTime);
        this.avgTime = roundTwoDecimals(sumTime / individuals.size());
        this.makespan = roundTwoDecimals(makespan);
    }

    double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.parseDouble(twoDForm.format(d));
    }

    /**
     * Getters and setters.
     */
    public ArrayList<Individual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(ArrayList<Individual> individuals) {
        this.individuals = individuals;
    }
}

