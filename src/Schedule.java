import java.util.*;

/**
 * Describes the project definition and also the solution itself - a project schedule.
 * Consists of activities, resources and information about successors.
 */
public class Schedule {

    private Activity[] activities;
    private Resource[] resources;
    private int numSkills;

    public Schedule(InstanceLoader instanceLoader) {
        this.activities = instanceLoader.getActivities();
        this.resources = instanceLoader.getResources();
        this.numSkills = instanceLoader.getNumSkills();
        setEarliestStartTimeForActivities(false);
    }

    public Schedule(Schedule schedule, Activity[] activities) {
        numSkills = schedule.numSkills;
        this.activities = activities;
        this.resources = schedule.resources;
    }

    /**
     * Sets starting time of activity in respect to precedence relations
     */
    private void setEarliestStartTimeForActivities(boolean addRandomTime) {
        for (Activity activity : activities) {
            int earliest = getEarliestTime(activity);
            activity.setStart(earliest);
        }

        if (addRandomTime) {
            int latestStartTime = 0;
            for (Activity activity : activities) {
                if (activity.getStart() > latestStartTime) {
                    latestStartTime = activity.getStart();
                }
            }

            Random random = new Random();
            for (Activity activity : activities) {
                activity.setStart(activity.getStart() + random.nextInt(5));
            }
        }
    }


    /**
     * Calculates the earliest possible time in which given activity
     * can be started i.e. the time, when
     * the last of its predecessors have finished.
     */
    public int getEarliestTime(Activity activity) {
        int earliest = 0;
        Set<Integer> predecessors = activity.getPredecessors();
        if (predecessors != null) {
            for (int p : predecessors) {
                Activity pred = getActivity(p);
                int predFinish = pred.getStart() + pred.getDuration();
                if (predFinish > earliest) {
                    earliest = predFinish + 1;
                }
            }
        }
        return earliest;
    }

    /**
     * Assigns resource to the activity for given skill and updates state of the resource
     */
    public void assign(Activity activity, Resource resource, Skill skill) {
        skill.setResourceId(resource.getId());
        updateResource(activity, resource, skill.getType());
    }

    /**
     * Updates status of the skill reserved by assigning id and sets the current activity taking the resource
     */
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

    /**
     * Shift start time of activity if no available resources found at that moment
     * and shift depending on possible new finish times of predecessors
     */
    public void shiftStartTimeForActivity(Activity activity, int resourceFinishTime) {
        Set<Integer> predecessors = activity.getPredecessors();
        int newStart = activity.getStart();
        for (int p : predecessors) {
            Activity predecessor = getActivity(p);
            int predecessorFinish = predecessor.getStart() + predecessor.getDuration();
            if (newStart <= predecessorFinish) {
                newStart = predecessorFinish + 1;
            }
        }
        if (resourceFinishTime > newStart) {
            newStart = resourceFinishTime + 1;
        }
        activity.setStart(newStart);
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
     * available at the given time.
     * (could be used for optimization)
     */
    public HashMap<Integer, List<Resource>> getAvailableResourcesForSkills(Activity activity, int time) {
        HashMap<Integer, List<Resource>> resourcesPerSkills = new HashMap<>();
        for (int i = 0; i < numSkills; i++) {
            resourcesPerSkills.put(i, new ArrayList<>());
            for (Resource resource : resources) {
                if (resource.hasAvailableSkill(activity, i) && time >= resource.getFinish()) {
                    resourcesPerSkills.get(i).add(resource);
                }
            }
        }
        return resourcesPerSkills;
    }

    /**
     * Finds a resource from the list freeResources with the earliest
     * finish time of its work
     * (could be used for Greedy approach)
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
     * Cleans time for resources and activities.
     */
    public void cleanAll() {
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

    public void cleanResources() {
        for (Resource resource : resources) {
            for (Skill skill : resource.getSkills()) {
                if (skill != null) {
                    skill.setResourceId(-1);
                }
            }
            resource.setFinish(-1);
        }
    }

    public void cleanActivities() {
        for (Activity activity : activities) {
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
}
