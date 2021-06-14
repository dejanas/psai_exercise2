import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Performs GeneticAlgorithm to the MSPSP instance with default or given parameters.
 */
public class GeneticAlgorithm {

    private static final Logger LOGGER = Logger.getLogger(GeneticAlgorithm.class.getName());

    private int popSize;
    private int generations;
    private double mutationProbability;
    private double crossoverProbability;
    private Integer tournamentSize;
    private Population newPopulation;
    private String filename;
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
        this.filename = filename;
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
            Schedule schedule = randomInitializeSchedule();
            // validateSchedule(schedule);
            individuals.add(initializeIndividual(schedule));
        }
        return new Population(individuals, 0);
    }

    /**
     * Random initialize schedule. Random assign activities to capable resources.
     */
    Schedule randomInitializeSchedule() {
        Schedule initialSchedule = new Schedule(loadInstance(filename));
        Activity[] activities = initialSchedule.getActivities();
        Random generator = new Random();

        for (Activity activity : activities) {
            setEarliestStartTimeForActivity(initialSchedule, activity);
            Set<Resource> assignedResources = new HashSet<>();

            RequiredSkill[] requiredSkills = activity.getRequiredSkills();
            for (RequiredSkill requiredSkill : requiredSkills) {
                if (requiredSkill.getRequired() > 0) {
                    for (Skill skill : requiredSkill.getSkills()) {
                        HashMap<Integer, List<Resource>> resourcesForSkills = initialSchedule.getAvailableResourcesForSkills(activity);
                        List<Resource> capableResources = resourcesForSkills.get(requiredSkill.getType());

                        if (capableResources.size() == 0) {
                            resourcesForSkills = initialSchedule.getCurrentlyUnavailableResourcesForSkills(activity);
                            capableResources = resourcesForSkills.get(requiredSkill.getType());
                            if (capableResources.isEmpty()) {
                                LOGGER.log(Level.SEVERE, "No more available resources, something is wrong!");
                            }
                            Resource resource = capableResources.get(generator.nextInt(capableResources.size()));
                            shiftStartTimeForActivity(activity, resource.getFinish());
                            initialSchedule.assign(activity, resource, skill);
                            assignedResources.add(resource);
                        } else {
                            //TODO: also get resource with earlier finish time? greedy? improved?
                            Resource resource = capableResources.get(generator.nextInt(capableResources.size()));
                            initialSchedule.assign(activity, resource, skill);
                            assignedResources.add(resource);
                        }
                    }
                } else {
                    // TODO: add initial (dummy) activity
                }
            }
            setTime(activity, assignedResources);
        }
        return initialSchedule;
    }

    private void shiftStartTimeForActivity(Activity activity, int shift) {
        activity.setStart(shift + 1);
        //TODO: Add successors maybe?
//        for(Activity successor : activity.getSuccessors()){
//            successor.setStart(successor.getStart() + shift);
//        }
    }

    private Resource getResourceWithEarliestFinish(List<Resource> capableResources) {
        Resource minResource = capableResources.get(0);
        for (Resource resource : capableResources) {
            if (resource.getFinish() < minResource.getFinish()) {
                minResource = resource;
            }
        }
        return minResource;
    }

    /**
     * Sets starting time of activity to earliest possible depending on its predecessor relations.
     */
    private void setEarliestStartTimeForActivity(Schedule schedule, Activity activity) {
        int start = schedule.getEarliestTime(activity);
        activity.setStart(start);
    }

    /**
     * Reconfigures starting time of activity due to resources available for it.
     */
    private void setTime(Activity activity, Set<Resource> resourceSet) {
        int start = activity.getStart();
        for (Resource resource : resourceSet) {
            if (resource.getFinish() > start) {
                start = resource.getFinish();
            }
        }
        activity.setStart(start);
        for (Resource resource : resourceSet) {
            resource.setFinish(start + activity.getDuration());
        }
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

            //TODO: fix mutation step
            if (false/*shouldDoMutation()*/) {
                individual = mutate(individual);
            }

            Schedule schedule = individual.getSchedule();
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
        Set<Resource> assignedResources = new HashSet<>();

        for (RequiredSkill requiredSkill : requiredSkills) {
            if (requiredSkill.getRequired() > 0) {
                for (Skill skill : requiredSkill.getSkills()) {
                    HashMap<Integer, List<Resource>> resourcesForSkills = schedule.getAvailableResourcesForSkills(activityToMutate);
                    List<Resource> capableResources = resourcesForSkills.get(requiredSkill.getType());

                    if (capableResources.size() == 0) {
                        resourcesForSkills = schedule.getCurrentlyUnavailableResourcesForSkills(activityToMutate);
                        capableResources = resourcesForSkills.get(requiredSkill.getType());
                        if (capableResources.isEmpty()) {
                            LOGGER.log(Level.SEVERE, "No more available resources, something is wrong!");
                        }
                        Resource resource = getResourceWithEarliestFinish(capableResources);
                        shiftStartTimeForActivity(activityToMutate, resource.getFinish());
                        schedule.assign(activityToMutate, resource, skill);
                        assignedResources.add(resource);
                    } else {
                        //TODO: also get resource with earlier finish time? greedy? improved?
                        Resource resource = capableResources.get(generator.nextInt(capableResources.size()));
                        schedule.assign(activityToMutate, resource, skill);
                        assignedResources.add(resource);
                    }
                }
            } else {
                // TODO: add initial (dummy) activity
            }
            setTime(activityToMutate, assignedResources);
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
