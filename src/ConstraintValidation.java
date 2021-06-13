/**
 * Utility class for validating constraints on the Schedule.
 */
public class ConstraintValidation {

    private final Schedule schedule;

    public ConstraintValidation(Schedule schedule) {
        this.schedule = schedule;
    }

    public boolean validate() {
        return validateAssignmentConstraint(schedule)
                && validateConflictConstraint(schedule)
                && validatePrecedenceRelationConstraint(schedule)
                && validateSkillConstraint(schedule);
    }

    /**
     * Check if all activities have at least one resource assigned for each required skill.
     *
     */
    public boolean validateAssignmentConstraint(Schedule schedule) {
        boolean isValid = true;
        for (Activity activity : schedule.getActivities()) {
            Skill[] requiredSkills = activity.getRequiredSkills();
            for (Skill skill : requiredSkills) {
                if (skill.getResourceId() == -1) {
                    isValid = false;
                    break;
                }
            }
        }
        return isValid;
    }

    /**
     * Checks whether a resource exists, which is assigned to multiple activities
     * in the same period of time.
     *
     */
    public boolean validateConflictConstraint(Schedule schedule) {
        boolean isValid = true;
        Activity[] activities = schedule.getActivities();
        for (Activity firstActivity : activities) {
            for (Activity secondActivity : activities) {
                if (firstActivity.getId() != secondActivity.getId()) {
                    Skill[] firstActivitySkills = firstActivity.getRequiredSkills();
                    Skill[] secondActivitySkills = secondActivity.getRequiredSkills();
                    for (int i = 0; i < schedule.getNumSkills(); i++) {
                        if (firstActivitySkills[i].getResourceId() == secondActivitySkills[i].getResourceId() &&
                                firstActivity.getStart() <= secondActivity.getStart() &&
                                (firstActivity.getStart() + firstActivity.getDuration()) > secondActivity.getStart()) {
                            isValid = false;
                            break;
                        }
                    }
                }
            }
        }
        return isValid;
    }

    /**
     * Checks if relations constraint is satisfied.
     *
     */
    public boolean validatePrecedenceRelationConstraint(Schedule schedule) {
        boolean isValid = true;
        for (Activity activity : schedule.getActivities()) {
            if (activity.getStart() < schedule.getEarliestTime(activity)) {
                isValid = false;
            }
        }
        return isValid;
    }

    /**
     * Validates if schedule violates skill constraint.
     *
     */
    public boolean validateSkillConstraint(Schedule schedule) {
        boolean isValid = true;
        for (Activity activity : schedule.getActivities()) {
            Skill[] requiredSkills = activity.getRequiredSkills();
            for (Skill skill : requiredSkills) {
                if (!schedule.canDo(activity, schedule.getResource(skill.getResourceId()))) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }
}
