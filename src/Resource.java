import java.util.Arrays;


/**
 * Resource can be assigned to activity if it has one of the required skills.
 * After Schedule is created, resource is assigned with finish field - the time
 * when resource finished its last assigned activity.
 */
public class Resource {

    private int id;
    private Skill[] skills;
    private int finish;

    public Resource(int id, Skill[] skills, int finish) {
        this.id = id;
        this.skills = skills;
        this.finish = finish;
    }

    public Resource() {
        this(-1, null, -1);
    }

    public Resource(int id, Skill[] skills) {
        this.id = id;
        this.skills = skills;
    }

    public boolean hasAvailableSkill(Activity activity, int type) {
        return skills[type] != null && skills[type].getResourceId() == -1 && !hasContributed(activity, type);
    }

    public boolean isCapableOf(int type) {
        return skills[type] != null;
    }


    public boolean hasContributed(Activity activity, int type) {
        Skill skill = skills[type];
        if (skill != null && skill.getResourceId() == id) {
            RequiredSkill requiredSkill = activity.getRequiredSkills()[type];
            for (Skill activitySkill : requiredSkill.getSkills()) {
                if (activitySkill.getResourceId() == id) {
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
