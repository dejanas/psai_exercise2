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
    private double normalizedTime;

    private int numberOfConflicts;
    private int leastConflicts;
    private int mostConflicts;
    private int avgConflicts;

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
        return id + ";" + bestTime + ";" + worstTime + ";" + avgTime + ";" + avgConflicts +'\n';
    }

    void evaluateDuration() {
        double bestTime = 0;
        double worstTime = 0;
        double sumTime = 0;
        double normalizedTime = 1;

        for (Individual individual : individuals) {
            Evaluation evaluator = new Evaluation(individual.getSchedule());
            double duration = evaluator.getDuration();
            individual.setDuration(duration);
            sumTime += duration;
            if (duration < bestTime || 0 == bestTime) {
                bestTime = duration;
            }
            if (duration > worstTime || 0 == worstTime) {
                worstTime = duration;
            }
            double durationNormalized = evaluator.getDurationNormalized();
            if(durationNormalized < normalizedTime){
                normalizedTime = durationNormalized;
            }
        }
        this.bestTime = roundTwoDecimals(bestTime);
        this.worstTime = roundTwoDecimals(worstTime);
        this.sumTime = roundTwoDecimals(sumTime);
        this.avgTime = roundTwoDecimals(sumTime / individuals.size());
        this.normalizedTime = roundTwoDecimals(normalizedTime);
    }

    void evaluateConstraints(){
        int leastConflicts = 0;
        int mostConflicts = 0;
        int sumConflicts = 0;

        for (Individual individual : individuals) {
            Evaluation evaluator = new Evaluation(individual.getSchedule());
            int conflicts = evaluator.getNumberOfConflicts();
            individual.setConflicts(conflicts);
            sumConflicts += conflicts;
            if (conflicts < leastConflicts) {
                leastConflicts = conflicts;
            }
            if (conflicts > mostConflicts) {
                mostConflicts = conflicts;
            }
        }
        this.leastConflicts = leastConflicts;
        this.mostConflicts = mostConflicts;
        this.avgConflicts = sumConflicts/individuals.size();
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

