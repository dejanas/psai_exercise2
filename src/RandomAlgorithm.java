import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomAlgorithm {

    private static final Logger LOGGER = Logger.getLogger(GeneticAlgorithm.class.getName());


    /**
     * Perform scheduling: assign activities to resources by taking available resources randomly.
     */
    void schedule(Schedule schedule) {
        Activity[] activities = schedule.getActivities();
        Random generator = new Random();
        for (Activity activity : activities) {
            Set<Resource> assignedResources = new HashSet<>();

            RequiredSkill[] requiredSkills = activity.getRequiredSkills();
            for (RequiredSkill requiredSkill : requiredSkills) {
                if (requiredSkill.getRequired() > 0) {
                    for (Skill skill : requiredSkill.getSkills()) {
                        HashMap<Integer, List<Resource>> resourcesForSkills = schedule.getAvailableResourcesForSkills(activity);
                        List<Resource> capableResources = resourcesForSkills.get(requiredSkill.getType());

                        if (capableResources.size() == 0) {
                            resourcesForSkills = schedule.getCurrentlyUnavailableResourcesForSkills(activity);
                            capableResources = resourcesForSkills.get(requiredSkill.getType());
                            if (capableResources.isEmpty()) {
                                LOGGER.log(Level.SEVERE, "No more available resources, something is wrong!");
                            }
                            Resource resource = capableResources.get(generator.nextInt(capableResources.size()));
                            schedule.shiftStartTimeForActivity(activity, resource.getFinish());
                            schedule.assign(activity, resource, skill);
                            assignedResources.add(resource);
                        } else {
                            Resource resource = capableResources.get(generator.nextInt(capableResources.size()));
                            schedule.assign(activity, resource, skill);
                            assignedResources.add(resource);
                        }
                    }
                } else if (activity.getPredecessors() != null) {
                    schedule.shiftStartTimeForActivity(activity, 0);
                }
            }
            updateResourceFinishTime(activity, assignedResources);
        }
    }

    /**
     * Could be used for Greedy approach.
     */
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
     * Reconfigures starting time of resource by taking into the account the duration of the activity taking that resource.
     */
    private void updateResourceFinishTime(Activity activity, Set<Resource> resourceSet) {
        int start = activity.getStart();
        for (Resource resource : resourceSet) {
            resource.setFinish(start + activity.getDuration());
        }
    }
}
