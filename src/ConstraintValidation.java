import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for validating constraints on the Schedule
 */
public class ConstraintValidation {

    private final Schedule schedule;

    public ConstraintValidation(Schedule schedule) {
        this.schedule = schedule;
    }

    public boolean validate() {
        return validateAssignmentConstraint() == 0
                && validateConflictConstraint() == 0
                && validatePrecedenceRelationConstraint() == 0
                && validateSkillConstraint() == 0;
    }

    public int getNumberOfConflicts() {
        return validateAssignmentConstraint() +
                validateConflictConstraint() +
                validatePrecedenceRelationConstraint() +
                validateSkillConstraint();
    }

    /**
     * Check if all activities have at least one resource assigned for each required skill.
     */
    private int validateAssignmentConstraint() {
        int conflictsFound = 0;
        for (Activity activity : schedule.getActivities()) {
            RequiredSkill[] requiredSkills = activity.getRequiredSkills();
            for (RequiredSkill skill : requiredSkills) {
                if (skill.getRequired() != skill.getAssigned()) {
                    conflictsFound++;
                }
            }
        }
        return conflictsFound;
    }

    /**
     * Checks whether a resource exists, which is assigned to multiple activities
     * in the same period of time.
     */
    private int validateConflictConstraint() {
        int conflictsFound = 0;
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
                            conflictsFound++;
                        }
                    }
                }
            }
        }
        return conflictsFound;
    }

    private boolean haveSameResourceAssigned(RequiredSkill[] firstActivitySkills, RequiredSkill[] secondActivitySkills) {
        int numSkills = firstActivitySkills.length;
        for (int i = 0; i < numSkills; i++) {
            Skill[] firstActivitySkillsByType = firstActivitySkills[i].getSkills();
            Skill[] secondActivitySkillsByType = secondActivitySkills[i].getSkills();
            Set<Integer> firstActivitySkillsResourceSet = Arrays.stream(firstActivitySkillsByType)
                    .map(Skill::getResourceId)
                    .collect(Collectors.toSet());
            Set<Integer> secondActivitySkillsResourceSet = Arrays.stream(secondActivitySkillsByType)
                    .map(Skill::getResourceId)
                    .collect(Collectors.toSet());
            if (firstActivitySkillsResourceSet.isEmpty() || secondActivitySkillsResourceSet.isEmpty()) {
                return false;
            }
            Set<Integer> intersection = new HashSet<>(firstActivitySkillsResourceSet);
            intersection.retainAll(secondActivitySkillsResourceSet);
            if (!intersection.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if relations constraint is satisfied.
     */
    private int validatePrecedenceRelationConstraint() {
        int conflictsFound = 0;
        for (Activity activity : schedule.getActivities()) {
            if (activity.getStart() < schedule.getEarliestTime(activity)) {
                conflictsFound++;
            }
        }
        return conflictsFound;
    }

    /**
     * Validates if schedule violates skill constraint.
     */
    private int validateSkillConstraint() {
        int conflictsFound = 0;
        for (Activity activity : schedule.getActivities()) {
            RequiredSkill[] requiredSkills = activity.getRequiredSkills();
            for (RequiredSkill requiredSkill : requiredSkills) {
                for (Skill skill : requiredSkill.getSkills()) {
                    if (!schedule.getResource(skill.getResourceId()).isCapableOf(skill.getType())) {
                        conflictsFound++;
                    }
                }
            }
        }
        return conflictsFound;
    }
}
