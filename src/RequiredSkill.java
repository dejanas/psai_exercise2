/**
 * Contains an array of Skills of given type required for the certain activity.
 */
public class RequiredSkill {

    private int type;
    private int required;
    private Skill[] skills;

    public RequiredSkill(int type, int required) {
        this.type = type;
        this.required = required;
        this.skills = new Skill[required];
        for(int i = 0; i < required; i++){
            skills[i] = new Skill(type);
        }
    }

    public RequiredSkill(int type, int required, Skill[] skills) {
        this.type = type;
        this.required = required;
        this.skills = skills;
    }

    /**
     * Getters and setters.
     */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRequired() {
        return required;
    }

    public void setRequired(int required) {
        this.required = required;
    }

    public Skill[] getSkills() {
        return skills;
    }

    public void setSkills(Skill[] skills) {
        this.skills = skills;
    }

    public int getAssigned() {
        int assigned = 0;
        for (Skill skill : skills) {
            if (skill.getResourceId() != -1) {
                assigned++;
            }
        }
        return assigned;
    }
}
