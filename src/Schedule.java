import java.util.*;

/**
 * Describes the project definition and also the solution itself - a project schedule.
 * Consists of activities, resources and information about successors.
 */
public class Schedule {

    private Activity[] activities;
    private Resource[] resources;
    private int numSkills;
    private boolean[] hasSuccessors;

    public Schedule(InstanceLoader instanceLoader) {
        this.activities = instanceLoader.getActivities();
        this.resources = instanceLoader.getResources();
        this.numSkills = instanceLoader.getNumSkills();
        this.hasSuccessors = instanceLoader.getHasSuccessors();
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
        skill.setResourceId(resource.getId());
        updateResource(activity, resource, skill.getType());
        // TODO: create Assignment object for output purpose
        // return Assignment(activity, resource, skill);
    }

    private void updateResource(Activity activity, Resource resource, int skillType) {
        Skill[] resourceSkills = resource.getSkills();
        for (Skill resourceSkill : resourceSkills) {
            if (resourceSkill != null && resourceSkill.getType() == skillType) {
                resourceSkill.setResourceId(resource.getId());
                break;
            }
        }
        resource.setCurrentActivityId(activity.getId());
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
     * Finds all resources capable and available of doing given activity for different skills,
     */
    public HashMap<Integer, List<Resource>> getAvailableResourcesForSkills(Activity activity) {
        HashMap<Integer, List<Resource>> resourcesPerSkills = new HashMap<>();
        for (int i = 0; i < numSkills; i++) {
            resourcesPerSkills.put(i, new ArrayList<>());
            for (Resource resource : resources) {
                if (resource.hasAvailableSkill(activity, i)) {
                    resourcesPerSkills.get(i).add(resource);
                }
            }
        }
        return resourcesPerSkills;
    }

    /**
     * Finds all resources capable but currently unavailable of doing given activity for different skills.
     */
    public HashMap<Integer, List<Resource>> getCurrentlyUnavailableResourcesForSkills(Activity activity) {
        HashMap<Integer, List<Resource>> resourcesPerSkills = new HashMap<>();
        for (int i = 0; i < numSkills; i++) {
            resourcesPerSkills.put(i, new ArrayList<>());
            for (Resource resource : resources) {
                if (resource.hasCurrentlyUnavailableSkill(activity, i)) {
                    resourcesPerSkills.get(i).add(resource);
                }
            }
        }
        return resourcesPerSkills;
    }

    /**
     * Finds all resources capable of doing given activity for different skills,
     * available at the given timestamp.
     */
    public HashMap<Integer, List<Resource>> getAvailableResourcesForSkills(Activity activity, int timestamp) {
        HashMap<Integer, List<Resource>> resourcesPerSkills = new HashMap<>();
        for (int i = 0; i < numSkills; i++) {
            resourcesPerSkills.put(i, new ArrayList<>());
            for (Resource resource : resources) {
                if (resource.hasAvailableSkill(activity, i) && timestamp >= resource.getFinish()) {
                    resourcesPerSkills.get(i).add(resource);
                }
            }
        }
        return resourcesPerSkills;
    }

    /**
     * Finds a resource from the list freeResources with the earliest
     * finish time of its work.
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
     */
    public int getEarliestTime(Activity activity) {
        int earliest = 0;
        Set<Integer> predecessors = activity.getPredecessors();
        if (predecessors != null) {
            for (int p : predecessors) {
                Activity pred = getActivity(p);
                if (pred.getStart() + pred.getDuration() > earliest) {
                    earliest = pred.getStart() + pred.getDuration();
                }
            }
        }
        return earliest;
    }

    /**
     * Clears timestamps from activities and resources.
     */
    public void clear() {
        for (Activity activity : activities) {
            activity.setStart(-1);
            for (RequiredSkill requiredSkill : activity.getRequiredSkills()) {
                if (requiredSkill.getRequired() > 0) {
                    for (Skill skill : requiredSkill.getSkills()) {
                        if (skill != null) {
                            skill.setResourceId(-1);
                        }
                    }
                }
            }
        }
        for (Resource resource : resources) {
            for (Skill skill : resource.getSkills()) {
                skill.setResourceId(-1);
            }
            resource.setFinish(-1);
        }
    }

    /**
     * Getters and setters.
     */
    public Activity[] getActivities() {
        return activities;
    }

    public void setActivities(Activity[] activities) {
        this.activities = activities;
    }

    public Resource[] getResources() {
        return resources;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public int getNumSkills() {
        return numSkills;
    }

    public void setNumSkills(int numSkills) {
        this.numSkills = numSkills;
    }
}
