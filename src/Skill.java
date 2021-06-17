/**
 * Skill links resource and activity by resourceId field of a Skill and RequiredSkill.
 * If resource is not still assigned, resourceId is -1.
 */
public class Skill {

    private int type;
    private int resourceId;

    public Skill(int type) {
        this.type = type;
        this.resourceId = -1;
    }

    public String toString() {
        return String.valueOf(type);
    }

    @Override
    public boolean equals(Object s) {
        if (!(s instanceof Skill)) {
            return false;
        }
        Skill skill = (Skill) s;
        return skill.type == type;
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

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}
