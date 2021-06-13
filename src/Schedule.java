import java.util.*;

/**
 * Describes the project definition and also the solution itself - project
 * schedule. Consists of tasks, resources and an evaluation. Able to build a
 * schedule from provided representation and manage schedule.
 */
public class Schedule {

    private Activity[] activities;
    private Resource[] resources;
    private int numSkills;
    private boolean[] hasSuccessors;

    private Evaluation evaluation;

    public Schedule(InstanceLoader instanceLoader) {
        this.activities = instanceLoader.getActivities();
        this.resources = instanceLoader.getResources();
        this.numSkills = instanceLoader.getNumSkills();
        this.hasSuccessors = instanceLoader.getHasSuccessors();
        clear(true);
    }

    public Schedule(Schedule schedule) {
        activities = new Activity[schedule.getActivities().length];
        for (int i = 0; i < activities.length; i++) {
            Activity org = schedule.getActivities()[i];
            activities[i] = new Activity(org.getId(), org.getRequiredSkills(),
                    org.getDuration(), org.getPredecessors());
        }
        resources = new Resource[schedule.getResources().length];
        for (int i = 0; i < resources.length; i++) {
            Resource org = schedule.getResources()[i];
            resources[i] = new Resource(org.getId(), org.getSkills());
        }
        for (Activity activity : activities) {
            activity.setStart(schedule.getActivity(activity.getId()).getStart());
            activity.setRequiredSkills(schedule.getActivity(activity.getId()).getRequiredSkills());
        }
        for (Resource resource : resources) {
            resource.setFinish(schedule.getResource(resource.getId()).getFinish());
        }
        numSkills = schedule.numSkills;
        hasSuccessors = schedule.hasSuccessors;
    }

    /**
     * Assigns resource to the activity for given skill. Does not
     * assign time. Does not check if the assignment violates the constraints.
     */
    public void assign(Activity activity, Resource resource, Skill skill) {
        int availability = skill.getAvailability();
        skill.setAvailability(availability - 1);
        activity.setResourceForSkill(resource.getId(), skill.getType());
    }

    /**
     * Clears timestamps from tasks and resources.
     * and optionally activity - resource assignments
     *
     * @param withAssignments determines whether to clear assignments
     */
    public void clear(boolean withAssignments) {
        if (withAssignments) {
            for (Activity activity : activities) {
                activity.setStart(-1);
                for (Skill skill : activity.getRequiredSkills()) {
                    skill.setResourceId(-1);
                }
            }
        } else {
            for (Activity activity : activities) {
                activity.setStart(-1);
            }
        }
        for (Resource resource : resources) {
            resource.setFinish(-1);
        }
    }

    public Activity getActivity(int activityId) {
        for (Activity activity : activities) {
            if (activity.getId() == activityId) {
                return activity;
            }
        }
        return null;
    }

    public Resource getResource(int resourceId) {
        for (Resource r : resources) {
            if (r.getId() == resourceId) {
                return r;
            }
        }
        return null;
    }

    /**
     * Finds all resources capable of doing given activity
     */
    public List<Resource> getCapableResources(Activity activity) {
        List<Resource> result = new LinkedList<>();
        for (Resource resource : resources) {
            if (canDo(activity, resource)) {
                result.add(resource);
            }
        }
        return result;
    }

    /**
     * Finds all resources capable of doing given activity
     */
    public HashMap<Integer, List<Resource>> getCapableResourcesForSkills() {
        HashMap<Integer, List<Resource>> resourcesPerSkills = new HashMap<>();
        for (int i = 0; i < numSkills; i++) {
            resourcesPerSkills.put(i, new ArrayList<>());
            for (Resource resource : resources) {
                if (resource.hasSkill(i)) {
                    resourcesPerSkills.get(i).add(resource);
                }
            }
        }
        return resourcesPerSkills;
    }

    /**
     * Finds all resources capable of doing given activity and
     * available at the given timestamp
     */
    public List<Resource> getCapableResources(Activity activity, int timestamp) {
        List<Resource> result = new LinkedList<>();
        for (Resource resource : resources) {
            if (canDo(activity, resource) && timestamp >= resource.getFinish()) {
                result.add(resource);
            }
        }
        return result;
    }

    /**
     * Finds a resource from the list <code>freeResources</code> with the earliest
     * finish time of its work.
     *
     * @param freeResources list of resources
     * @return Resource with the earliest finish time of work
     */
    public Resource findFirstFreeResource(List<Resource> freeResources) {
        if (freeResources == null) {
            return null;
        }
        Resource result = freeResources.get(0);
        int firstFree = freeResources.get(0).getFinish();
        for (Resource r : freeResources) {
            if (r.getFinish() < firstFree) {
                result = r;
                firstFree = r.getFinish();
            }
        }
        return result;
    }

    /**
     * Calculates the earliest possible time, in which activity
     * given can be started. It is the time, when
     * the last of its predecessors gets finished.
     *
     * @param activity activity, for which we want to find the
     *                 earliest time of start
     * @return finish time of the last predecessor of activity
     */
    public int getEarliestTime(Activity activity) {
        int earliest = 0;
        Set<Integer> pred = activity.getPredecessors();
        if (pred != null) {
            for (int p : pred) {
                Activity t = getActivity(p);
                if (t.getStart() + t.getDuration() > earliest) {
                    earliest = t.getStart() + t.getDuration();
                }
            }
        }
        return earliest;
    }

    /**
     * Gets all activities, which given resource can do.
     *
     * @param r resource, for which we look for activities, that it can do
     * @return activities doable by r
     */
    public List<Activity> tasksCapableByResource(Resource r) {
        List<Activity> activities = new LinkedList<>();
        for (Activity t : getActivities()) {
            if (canDo(t, r)) {
                activities.add(t);
            }
        }
        return activities;
    }

    public boolean canDo(Activity activity, Resource resource) {
        if (resource == null) {
            return false;
        }
        return resource.hasSkill(activity.getRequiredSkills());
    }

    /**
     * Getters and setters.
     */
    public Activity[] getActivities() {
        return activities;
    }

    public void setTasks(Activity[] activities) {
        this.activities = activities;
    }

    public Resource[] getResources() {
        return resources;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public int getNumSkills() {
        return numSkills;
    }

    public void setNumSkills(int numSkills) {
        this.numSkills = numSkills;
    }

    public boolean[] getHasSuccessors() {
        return hasSuccessors;
    }

    public void setHasSuccessors(boolean[] hasSuccessors) {
        this.hasSuccessors = hasSuccessors;
    }
}
