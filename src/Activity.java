import java.util.Arrays;
import java.util.Set;


/**
 * Activity is described by skills required, duration and predecessors (precedence relations).
 * After scheduling, activity stores information about resources assigned to it and start time. (-1 if not assigned)
 */
public class Activity implements Comparable {

    private final int id;
    private RequiredSkill[] requiredSkills;
    private int duration;
    private int start;
    private Set<Integer> predecessors;

    public Activity(int id, RequiredSkill[] requiredSkills, int duration, int start,
                    Set<Integer> predecessors) {
        this.id = id;
        this.requiredSkills = requiredSkills;
        this.duration = duration;
        this.start = start;
        this.predecessors = predecessors;
    }

    public Activity(int id, RequiredSkill[] requiredSkills, int duration, Set<Integer> predecessors) {
        this(id, requiredSkills, duration, -1, predecessors);
    }

    @Override
    public boolean equals(Object a) {
        if (!(a instanceof Activity)) {
            return false;
        }
        Activity activity = (Activity) a;
        return duration == activity.duration &&
                id == activity.id &&
                predecessors.equals(activity.predecessors) &&
                Arrays.equals(requiredSkills, activity.requiredSkills);
    }

    /**
     * Compares start times of activities.
     * <p>
     * Returns -1 if this activity starts earlier,
     * 1 if given activity starts earlier
     * 0 if they start at the same time
     */
    @Override
    public int compareTo(Object activity) {
        if (!(activity instanceof Activity)) {
            throw new IllegalArgumentException("Invalid type comparison");
        }
        return Integer.compare(start, ((Activity) activity).start);
    }

    public String toString() {
        StringBuilder p = new StringBuilder();
        for (int i : predecessors) {
            p.append(i).append(" ");
        }
        StringBuilder s = new StringBuilder();
        for (RequiredSkill i : requiredSkills) {
            s.append(i).append(" ");
        }
        return id + ", duration: " + duration + ", start: " + start
                + ", required skills: " + s
                + ", predecessors: " + p;
    }

    /**
     * Getters and setters.
     */
    public int getId() {
        return id;
    }

    public RequiredSkill[] getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(RequiredSkill[] requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public Set<Integer> getPredecessors() {
        return predecessors;
    }

    public void setPredecessors(Set<Integer> predecessors) {
        this.predecessors = predecessors;
    }
}
