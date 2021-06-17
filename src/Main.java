import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Main {
    private static final String INSTANCE_NAME = "inst_set1a_sf0.5_nc1.5_n20_m10_00";
    private static final String TEST_INSTANCE = "instances/" + INSTANCE_NAME + ".dzn";

    public static void main(String[] args) {
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(TEST_INSTANCE);
        int generation = 0;
        Population population = geneticAlgorithm.initializePopulation();
        population.evaluateDuration();
        population.evaluateConstraints();
        try {
            PrintWriter pw = new PrintWriter("results/res_" + INSTANCE_NAME + ".csv");
            StringBuilder sb = new StringBuilder();
            sb.append("pid");
            sb.append(';');
            sb.append("best");
            sb.append(';');
            sb.append("worst");
            sb.append(';');
            sb.append("avg");
            sb.append(';');
            sb.append("avgConflicts");
            sb.append('\n');

            sb.append(population);

            while (generation < geneticAlgorithm.getGenerations()) {
                generation++;
                population = geneticAlgorithm.createNewPopulation(population, generation);
                population.evaluateDuration();
                population.evaluateConstraints();
                sb.append(population);
            }
            pw.write(sb.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
