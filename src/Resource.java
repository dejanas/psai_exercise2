import java.util.Arrays;


/**
 * Resource can be assigned to activity if it has one of the required skills.
 * After Schedule is created, resource is assigned with finish field - the time
 * when resource finished its last assigned activity.
 */
public class Resource {

    private final int id;
    private Skill[] skills;
    private int finish;
    private int currentActivityId;

    public Resource(int id, Skill[] skills) {
        this.id = id;
        this.skills = skills;
        this.finish = -1;
        this.currentActivityId = -1;
    }

    public boolean hasAvailableSkill(Activity activity, int type) {
        boolean hasAvailableSkill = skills[type] != null && !hasContributed(activity);
        if (!hasAvailableSkill) return false;
        if (currentActivityId != -1) { //already assigned to some activity
            hasAvailableSkill = finish < activity.getStart();
        }
        return hasAvailableSkill;
    }

    public boolean hasCurrentlyUnavailableSkill(Activity activity, int type) {
        boolean hasSkill = skills[type] != null && !hasContributed(activity);
        if (!hasSkill) return false;
        if (currentActivityId != -1) { //already assigned to some activity
            return finish >= activity.getStart();
        }
        return false;
    }

    public boolean isCapableOf(int type) {
        return skills[type] != null;
    }

    /**
     * Checks if resource has already contributed to given activity.
     */
    public boolean hasContributed(Activity activity) {
        RequiredSkill[] requiredSkills = activity.getRequiredSkills();
        for (RequiredSkill requiredSkill : requiredSkills) {
            for (Skill reqSkill : requiredSkill.getSkills()) {
                if (reqSkill.getResourceId() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Getters and setters.
     */
    public int getId() {
        return id;
    }

    public Skill[] getSkills() {
        return skills;
    }

    public void setSkills(Skill[] skills) {
        this.skills = skills;
    }

    public int getFinish() {
        return finish;
    }

    public void setFinish(int finish) {
        this.finish = finish;
    }

    public int getCurrentActivityId() {
        return currentActivityId;
    }

    public void setCurrentActivityId(int currentActivityId) {
        this.currentActivityId = currentActivityId;
    }

    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Skill s : skills) {
            res.append(s).append(" ");
        }
        return id + ", " + res;
    }

    @Override
    public boolean equals(Object r) {
        if (!(r instanceof Resource)) {
            return false;
        }
        Resource resource = (Resource) r;
        return id == resource.id &&
                Arrays.equals(skills, resource.skills);
    }
}
