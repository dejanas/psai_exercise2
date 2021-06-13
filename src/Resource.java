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

    public boolean hasSkill(int type){
        return skills[type] != null && skills[type].getAvailability() == 1;
    }

    public boolean hasSkill(Skill[] requiredSkills) {
        boolean noRequiredSkills = true;
        for (int i = 0; i < requiredSkills.length; i++) {
            if (requiredSkills[i] != null) {
                noRequiredSkills = false;
                if (skills[i] != null) {
                    int available = skills[i].getAvailability();
                    if (available > 0) {
                        skills[i].setAvailability(available - 1);
                        return true;
                    }
                }
            }
        }
        return noRequiredSkills;
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
