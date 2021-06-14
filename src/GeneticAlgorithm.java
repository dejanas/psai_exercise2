import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


/**
 * Performs GeneticAlgorithm to the MSPSP instance with default or given parameters.
 */
public class GeneticAlgorithm {
    private int popSize;
    private int generations;
    private double mutationProbability;
    private double crossoverProbability;
    private Integer tournamentSize;
    private InstanceLoader instanceLoader;
    private Population newPopulation;
    private static final int DEFAULT_POP_SIZE = 300;
    private static final int DEFAULT_GENERATIONS = 100;
    private static final double DEFAULT_MUTATION_PROBABILITY = 0.01;
    private static final double DEFAULT_CROSSOVER_PROBABILITY = 0.1;
    private static final int DEFAULT_TOURNAMENT_SIZE = 5;

    /**
     * Constructor with parameters.
     *
     * @param popSize              - population size
     * @param generations          - the number of generations
     * @param mutationProbability  - mutation probability
     * @param crossoverProbability - crossover probability
     * @param filename             - test data filename
     */
    GeneticAlgorithm(int popSize, int generations, double mutationProbability, double crossoverProbability,
                     String filename, int tournamentSize) {
        this.popSize = popSize;
        this.generations = generations;
        this.mutationProbability = mutationProbability;
        this.crossoverProbability = crossoverProbability;
        this.tournamentSize = tournamentSize;
        loadInstance(filename);
    }

    /**
     * Default constructor.
     */
    GeneticAlgorithm(String filename) {
        this.popSize = DEFAULT_POP_SIZE;
        this.generations = DEFAULT_GENERATIONS;
        this.mutationProbability = DEFAULT_MUTATION_PROBABILITY;
        this.crossoverProbability = DEFAULT_CROSSOVER_PROBABILITY;
        this.tournamentSize = DEFAULT_TOURNAMENT_SIZE;
        loadInstance(filename);
    }

    /**
     * Load instance from the given filename.
     */
    public void loadInstance(String filename) {
        this.instanceLoader = new InstanceLoader(filename);
        instanceLoader.loadInstance();
    }

    /**
     * Initializes individual with its instance of Schedule.
     */
    Individual initializeIndividual(Schedule schedule) {
        return new Individual(schedule);
    }

    /**
     * Creates initial population with randomly created Schedule.
     */
    Population initializePopulation() {
        ArrayList<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < getPopSize(); i++) {
            Schedule schedule = randomInitializeSchedule();
            individuals.add(initializeIndividual(schedule));
        }
        return new Population(individuals, 0);
    }

    /**
     * Random initialize schedule. Random assign activities to capable resources.
     */
    Schedule randomInitializeSchedule() {
        Schedule initialSchedule = new Schedule(instanceLoader);
        Activity[] activities = initialSchedule.getActivities();
        Random generator = new Random();

        for (Activity activity : activities) {
            RequiredSkill[] requiredSkills = activity.getRequiredSkills();
            for (RequiredSkill requiredSkill : requiredSkills) {
                if (requiredSkill.getRequired() > 0) {
                    for (Skill skill : requiredSkill.getSkills()) {
                        HashMap<Integer, List<Resource>> resourcesForSkills = initialSchedule.getCapableResourcesForSkills(activity);
                        List<Resource> capableResources = resourcesForSkills.get(requiredSkill.getType());
                        Resource resource = capableResources.get(generator.nextInt(capableResources.size()));
                        initialSchedule.assign(resource, skill);
                    }
                } else {
                    // TODO: check
                }
            }
        }
        return assignTimestamps(initialSchedule);
    }

    /**
     * Assign time to activities and resources by using Greedy approach.
     */
    Schedule assignTimestamps(Schedule schedule) {
        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(instanceLoader.getHasSuccessors());
        schedule.setEvaluation(new Evaluation(schedule));
        greedyAlgorithm.assignTimestamps(schedule);
//        validateSchedule(schedule);
        return schedule;
    }

    //TODO: call
    void validateSchedule(Schedule schedule) {
        ConstraintValidation validator = new ConstraintValidation(schedule);
        validator.validate();
    }

    Population createNewPopulation(Population population, int id) {
        setNewPopulation(new Population(id));
        int currentIndividual = 0;

        while (currentIndividual < getPopSize()) {
            Individual individual = select(population);

            if (shouldDoCrossover()) {
                Individual parent2 = select(population);
                individual = crossover(individual, parent2);
            }

            if (shouldDoMutation()) {
                individual = mutate(individual);
            }

            Schedule schedule = assignTimestamps(individual.getSchedule());
            newPopulation.addNewIndividual(initializeIndividual(schedule));
            currentIndividual++;
        }

        return newPopulation;
    }

    /**
     * Selection step: select winner with tournament selection
     */
    Individual select(Population population) {
        Selection selection = new Selection(getTournamentSize(), population);
        return new Individual(selection.tournament());
    }

    /**
     * Recombination step: generate child from two parent individuals with crossover method
     */
    Individual crossover(Individual parent1, Individual parent2) {
        Activity[] parent1activities = parent1.getSchedule().getActivities();
        Activity[] parent2Activities = parent2.getSchedule().getActivities();

        Random generator = new Random();
        int crossoverPoint = generator.nextInt(parent1activities.length);
        Activity[] childActivities = new Activity[parent1activities.length];

        int currentActivity = 0;
        while (currentActivity < childActivities.length) {
            if (currentActivity < crossoverPoint) {
                childActivities[currentActivity] = parent1activities[currentActivity];
            } else {
                childActivities[currentActivity] = parent2Activities[currentActivity];
            }
            currentActivity++;
        }
        Schedule child = parent1.getSchedule();
        child.setTasks(childActivities);
        return new Individual(child);
    }

    /**
     * Mutatation step: mutate one activity to use different recourses
     */
    Individual mutate(Individual individual) {
        Schedule schedule = individual.getSchedule();
        Activity[] activities = schedule.getActivities();
        Random generator = new Random();
        Activity activityToMutate = activities[generator.nextInt(activities.length)];
        RequiredSkill[] requiredSkills = activityToMutate.getRequiredSkills();
        for (RequiredSkill requiredSkill : requiredSkills) {
            if (requiredSkill.getRequired() > 0) {
                for (Skill skill : requiredSkill.getSkills()) {
                    HashMap<Integer, List<Resource>> resourcesForSkills = schedule.getCapableResourcesForSkills(activityToMutate);
                    List<Resource> capableResources = resourcesForSkills.get(requiredSkill.getType());
                    Resource resource = capableResources.get(generator.nextInt(capableResources.size()));
                    schedule.assign(resource, skill);
                }
            } else {
                // TODO: check
            }
        }
        return individual;
    }

    boolean shouldDoCrossover() {
        return Math.random() > 1.0 - crossoverProbability;
    }

    boolean shouldDoMutation() {
        return Math.random() > 1.0 - mutationProbability;
    }

    /**
     * Getters and setters.
     */
    public int getPopSize() {
        return popSize;
    }

    public void setPopSize(int popSize) {
        this.popSize = popSize;
    }

    public int getGenerations() {
        return generations;
    }

    public Integer getTournamentSize() {
        return tournamentSize;
    }

    public void setTournamentSize(Integer tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    public Population getNewPopulation() {
        return newPopulation;
    }

    public void setNewPopulation(Population newPopulation) {
        this.newPopulation = newPopulation;
    }
}
