/**
 * Defines skill existing in a project. Skill is an element linking resource and
 * activity. Only a resource with corresponding skill available s.t. it is required for the activity
 * can be assigned to that activity.
 */
public class Skill {

    private int type;
    private int availability;
    private int resourceId;
    private int required;

    public Skill(int type) {
        this.type = type;
        this.availability = 1;
    }

    public Skill(int type, int required) {
        this.type = type;
        this.required = required;
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

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public int getRequired() {
        return required;
    }

    public void setRequired(int required) {
        this.required = required;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}
