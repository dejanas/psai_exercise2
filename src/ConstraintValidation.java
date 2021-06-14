import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for validating constraints on the Schedule.
 */
public class ConstraintValidation {

    private static final Logger LOGGER = Logger.getLogger(ConstraintValidation.class.getName());


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
     */
    public boolean validateAssignmentConstraint(Schedule schedule) {
        boolean isValid = true;
        for (Activity activity : schedule.getActivities()) {
            RequiredSkill[] requiredSkills = activity.getRequiredSkills();
            for (RequiredSkill skill : requiredSkills) {
                if (skill.getRequired() != skill.getAssigned()) {
                    LOGGER.log(Level.SEVERE, "Assignment constraint violated.");
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    /**
     * Checks whether a resource exists, which is assigned to multiple activities
     * in the same period of time.
     */
    public boolean validateConflictConstraint(Schedule schedule) {
        boolean isValid = true;
        Activity[] activities = schedule.getActivities();
        for (Activity firstActivity : activities) {
            for (Activity secondActivity : activities) {
                if (firstActivity.getId() != secondActivity.getId()) {
                    RequiredSkill[] firstActivitySkills = firstActivity.getRequiredSkills();
                    RequiredSkill[] secondActivitySkills = secondActivity.getRequiredSkills();
                    for (int i = 0; i < schedule.getNumSkills(); i++) {
                        if (firstActivity.getStart() <= secondActivity.getStart() &&
                                (firstActivity.getStart() + firstActivity.getDuration()) > secondActivity.getStart() &&
                                haveSameResourceAssigned(firstActivitySkills, secondActivitySkills)) {
                            isValid = false;
                            LOGGER.log(Level.SEVERE, "Conflict constraint violated.");
                        }
                    }
                }
            }
        }
        return isValid;
    }

    private boolean haveSameResourceAssigned(RequiredSkill[] firstActivitySkills, RequiredSkill[] secondActivitySkills) {
        int numSkills = firstActivitySkills.length;
        for (int i = 0; i < numSkills; i++) {
            Skill[] firstActivitySkillsByType = firstActivitySkills[i].getSkills();
            Skill[] secondActivitySkillsByType = secondActivitySkills[i].getSkills();
            for (Skill firstSkill : firstActivitySkillsByType) {
                for (Skill secondSkill : secondActivitySkillsByType) {
                    if (firstSkill.getResourceId() == secondSkill.getResourceId()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks if relations constraint is satisfied.
     */
    public boolean validatePrecedenceRelationConstraint(Schedule schedule) {
        boolean isValid = true;
        for (Activity activity : schedule.getActivities()) {
            if (activity.getStart() < schedule.getEarliestTime(activity)) {
                isValid = false;
                LOGGER.log(Level.SEVERE, "Precedence relation constraint violated.");
            }
        }
        return isValid;
    }

    /**
     * Validates if schedule violates skill constraint.
     */
    public boolean validateSkillConstraint(Schedule schedule) {
        boolean isValid = true;
        for (Activity activity : schedule.getActivities()) {
            RequiredSkill[] requiredSkills = activity.getRequiredSkills();
            for (RequiredSkill requiredSkill : requiredSkills) {
                for(Skill skill : requiredSkill.getSkills()){
                    if (!schedule.getResource(skill.getResourceId()).isCapableOf(skill.getType())) {
                        isValid = false;
                        LOGGER.log(Level.SEVERE, "Skill constraint violated.");
                    }
                }
            }
        }
        return isValid;
    }
}
