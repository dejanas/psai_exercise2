import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;


/**
 * Selection step: Tournament selection for GeneticAlgorithm
 */
public class Selection {
    private Integer tournamentSize;
    private final Population population;
    private Comparator<Individual> comparator;

    public Selection(Integer tournamentSize, Population population) {
        this.tournamentSize = tournamentSize;
        this.population = population;
        this.comparator = Comparator.comparingDouble(Individual::getFitness);
    }

    Individual tournament() {
        Random generator = new Random();
        ArrayList<Individual> individuals = population.getIndividuals();
        ArrayList<Individual> candidates = new ArrayList<>();
        int currentCandidate = 0;

        while (currentCandidate < tournamentSize) {
            Individual individual = individuals.get(generator.nextInt(individuals.size()));
            candidates.add(individual);
            currentCandidate++;
        }
        candidates.sort(comparator);
        return candidates.get(0);
    }
}

