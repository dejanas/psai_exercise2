import java.util.*;


/**
 * Performs GeneticAlgorithm to the MSPSP instance with default or given parameters.
 */
public class GeneticAlgorithm {

    private static final int DEFAULT_POP_SIZE = 300;
    private static final int DEFAULT_GENERATIONS = 100;
    private static final double DEFAULT_MUTATION_PROBABILITY = 0.01;
    private static final double DEFAULT_CROSSOVER_PROBABILITY = 0.1;
    private static final int DEFAULT_TOURNAMENT_SIZE = 5;

    private int popSize;
    private int generations;
    private double mutationProbability;
    private double crossoverProbability;
    private Population newPopulation;
    private String filename;
    private Integer tournamentSize;
    private RandomAlgorithm randomAlgorithm;

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
        this.filename = filename;
        this.randomAlgorithm = new RandomAlgorithm();
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
        this.filename = filename;
        this.randomAlgorithm = new RandomAlgorithm();
    }

    /**
     * Load instance from the given filename.
     */
    public InstanceLoader loadInstance(String filename) {
        InstanceLoader instanceLoader = new InstanceLoader(filename);
        instanceLoader.loadInstance();
        return instanceLoader;
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
            Schedule initialSchedule = initializeSchedule();
            randomAlgorithm.schedule(initialSchedule);
            individuals.add(initializeIndividual(initialSchedule));
        }
        return new Population(individuals, 0);
    }

    private Schedule initializeSchedule() {
        return new Schedule(loadInstance(filename));
    }

    private Schedule reinitializeSchedule(Schedule schedule, Activity[] activities) {
        return new Schedule(schedule, activities);
    }

    void validateSchedule(Schedule schedule) {
        ConstraintValidation validator = new ConstraintValidation(schedule);
        validator.validate();
    }

    Population createNewPopulation(Population population, int id) {
        setNewPopulation(new Population(id));
        int currentIndividual = 0;

        while (currentIndividual < getPopSize()) {
            Individual individual = select(population);
            Activity[] activities = individual.getSchedule().getActivities();
            Activity[] childActivities = null;

            if (shouldDoCrossover()) {
                Individual parent2 = select(population);
                Activity[] parent2activities = parent2.getSchedule().getActivities();
                childActivities = crossover(activities, parent2activities);
            }

            if (childActivities == null) {
                childActivities = activities;
            }

            if (shouldDoMutation()) {
                mutate(childActivities);
            }

            Schedule schedule = reinitializeSchedule(individual.getSchedule(), childActivities);

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
        return selection.tournament();
    }

    /**
     * Recombination step: generate child from two parent individuals with crossover method
     */
    Activity[] crossover(Activity[] parent1activities, Activity[] parent2activities) {
        Random generator = new Random();
        int crossoverPoint = generator.nextInt(parent1activities.length);

        Activity[] childActivities = new Activity[parent1activities.length];

        Set<Integer> alreadyAddedActivitiesById = new HashSet<>();
        int currentIndex = 0;
        while (currentIndex < crossoverPoint) {
            Activity activity = parent1activities[currentIndex];
            childActivities[currentIndex] = activity;
            alreadyAddedActivitiesById.add(activity.getId());
            currentIndex++;
        }

        while (currentIndex < childActivities.length) {
            Activity activity = parent2activities[currentIndex];
            if (!alreadyAddedActivitiesById.contains(activity.getId())) {
                childActivities[currentIndex] = activity;
                alreadyAddedActivitiesById.add(activity.getId());
            } else {
                childActivities[currentIndex] = null;
            }
            currentIndex++;
        }

        for (int i = 0; i < childActivities.length; i++) {
            Activity currentActivity = childActivities[i];
            if (currentActivity == null) {
                childActivities[i] = findMissingActivityInParentArray(childActivities, parent1activities);
            }
        }
        return childActivities;
    }

    /**
     * Populate missing activities in the array by taking the activity from one of the parents
     */
    private Activity findMissingActivityInParentArray(Activity[] childActivities, Activity[] parent1activities) {
        HashMap<Integer, Activity> childActivitiesById = new HashMap<>();
        for (Activity activity : childActivities) {
            if (activity != null) {
                childActivitiesById.put(activity.getId(), activity);
            }
        }
        for (int i = 1; i < childActivities.length + 1; i++) {
            if (!childActivitiesById.containsKey(i)) {
                for (Activity activity : parent1activities) {
                    if (activity.getId() == i) {
                        return activity;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Mutatation step: mutate one activity to have different starting time
     */
    void mutate(Activity[] activities) {

        Random generator = new Random();
        int positionToMutate = generator.nextInt(activities.length - 1);
        int positionToMutateWith = positionToMutate + 1;

        // try with swapping two start times of activities
        //        Activity tempActivity1 = activities[positionToMutate];
        //        Activity tempActivity2 = activities[positionToMutateWith];
        //        int tempStart1 = tempActivity1.getStart();
        //        tempActivity1.setStart(tempActivity2.getStart());
        //        tempActivity2.setStart(tempStart1);
        //        activities[positionToMutate] = tempActivity2;
        //        activities[positionToMutateWith] = tempActivity1;

        Activity activityToMutate = activities[positionToMutate];
        activityToMutate.setStart(activityToMutate.getStart() + 1);
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
