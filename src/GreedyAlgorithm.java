import java.util.HashSet;
import java.util.Set;


/**
 * Creates Schedule by applying the Greedy approach.
 */
public class GreedyAlgorithm {

    private final boolean[] hasSuccessors;

    public GreedyAlgorithm(boolean[] hasSuccessors) {
        this.hasSuccessors = hasSuccessors;
    }

    /**
     * Determines order of activities by setting their start
     * and finish time. Does not change activity / resource assignment.
     * Assumes that all assignments are set. Uses knowledge about
     * successors of each task to first place the activities with successors
     * and then rest of the tasks.
     */
    public void assignTimestamps(Schedule schedule) {
        Resource[] resources = schedule.getResources();
        for (Resource r : resources) {
            r.setFinish(0);
        }
        Activity[] activities = schedule.getActivities();
        for (int i = 0; i < activities.length; ++i) {
            if (hasSuccessors[i]) {
                assignTimestamp(activities[i], schedule);
            }
        }
        for (int i = 0; i < activities.length; ++i) {
            if (!hasSuccessors[i]) {
                assignTimestamp(activities[i], schedule);
            }
        }
    }

    private void assignTimestamp(Activity activity, Schedule schedule) {
        RequiredSkill[] requiredSkills = activity.getRequiredSkills();
        Set<Resource> resourceSet = new HashSet<>();
        for (RequiredSkill requiredSkill : requiredSkills) {
            if (requiredSkill.getRequired() > 0) {
                for (Skill skill : requiredSkill.getSkills()) {
                    resourceSet.add(schedule.getResource(skill.getResourceId()));
                    setTime(schedule, activity, resourceSet);
                }
            }
        }
    }

    private void setTime(Schedule schedule, Activity activity, Set<Resource> resourceSet) {
        int start = schedule.getEarliestTime(activity);
        for (Resource resource : resourceSet) {
            if (resource.getFinish() > start) {
                start = resource.getFinish();
            }
        }
        for (Resource resource : resourceSet) {
            resource.setFinish(start + activity.getDuration());
        }
    }
}
