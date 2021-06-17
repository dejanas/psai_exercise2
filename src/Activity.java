import java.util.Set;


/**
 * Activity is described by skills required, duration and predecessors (precedence relations).
 * After scheduling, activity stores information about resources assigned to it and start time. (-1 initially)
 */
public class Activity {

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

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (RequiredSkill i : requiredSkills) {
            s.append(i).append(" ");
        }
        StringBuilder p = new StringBuilder();
        if (predecessors != null) {
            for (int i : predecessors) {
                p.append(i).append(" ");
            }
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
