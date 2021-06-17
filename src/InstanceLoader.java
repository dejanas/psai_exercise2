import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Handles loading and parsing .dzn files for Multi Skill Project Scheduling Problem
 */
public class InstanceLoader {

    private static final Logger LOGGER = Logger.getLogger(InstanceLoader.class.getName());

    private final String filename;
    BufferedReader reader;
    String line;

    private int minMakespan;
    private int maxMakespan;

    private int numActivities;
    private int numSkills;
    private int numResources;

    private Resource[] resources;
    private Activity[] activities;

    HashMap<Integer, Set<Integer>> predecessors;

    public InstanceLoader(String filename) {
        this.filename = filename;
    }

    public void loadInstance() {
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.FINE, e.toString());
            return;
        }

        try {
            line = reader.readLine();

            minMakespan = readValue("mint");
            maxMakespan = readValue("maxt");

            numActivities = readValue("nActs");
            int[] duration = readDuration(numActivities);

            numSkills = readValue("nSkills");
            RequiredSkill[][] requiredSkills = readRequiredSkills(numActivities, numSkills);

            numResources = readValue("nResources");
            resources = readResources(numResources, numSkills);

            predecessors = readPredecessors();

            activities = readActivities(numActivities, requiredSkills, duration, predecessors);

        } catch (IOException e) {
            LOGGER.log(Level.FINE, e.toString());
        } finally {
            closeReader(reader);
        }
    }

    private void skipTo(String startsWith) throws IOException {
        while (null != line && !line.startsWith(startsWith)) {
            line = reader.readLine();
        }
    }

    private int readValue(String toRead) throws IOException {
        skipTo(toRead);
        if (null == line) {
            LOGGER.log(Level.FINE, String.format("No value specified for the given parameter %s.", toRead));
            return -1;
        }
        return Integer.parseInt(line.substring(line.lastIndexOf(' ') + 1, line.lastIndexOf(';')));
    }

    private Activity[] readActivities(int numActivities, RequiredSkill[][] requiredSkills,
                                      int[] duration, HashMap<Integer, Set<Integer>> predecessors) throws IOException {
        Activity[] activities = new Activity[numActivities];

        for (int i = 0; i < numActivities; i++) {
            activities[i] = new Activity(i + 1, requiredSkills[i], duration[i], predecessors.get(i + 1));
        }
        return activities;
    }

    private RequiredSkill[][] readRequiredSkills(int numActivities, int numSkills) throws IOException {
        RequiredSkill[][] requiredSkillsPerActivity = new RequiredSkill[numActivities][numSkills];

        String sreqArray = readMultilineArray("sreq");

        String[] sreqPerActivityArray = sreqArray.substring(sreqArray.indexOf('|') + 2,
                sreqArray.lastIndexOf('|'))
                .replace("\t", "")
                .split("\\|");

        for (int i = 0; i < numActivities; i++) {
            String[] requiredSkillsArray = sreqPerActivityArray[i].split(",");
            RequiredSkill[] requiredSkills = new RequiredSkill[numSkills];
            for (int j = 0; j < numSkills; ++j) {
                int required = Integer.parseInt(requiredSkillsArray[j].trim());
                requiredSkills[j] = new RequiredSkill(j, required);
            }
            requiredSkillsPerActivity[i] = requiredSkills;
        }
        return requiredSkillsPerActivity;
    }

    private String readMultilineArray(String skipTo) throws IOException {
        skipTo(skipTo);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(line);
        boolean endOfArray = false;
        while (!endOfArray) {
            line = reader.readLine();
            stringBuilder.append(line);
            endOfArray = line.endsWith("|];");
        }
        return stringBuilder.toString();
    }

    private int[] readDuration(int numActivities) throws IOException {
        skipTo("dur");

        int[] durationPerActivity = new int[numActivities];
        String[] durationPerActivityArray = getArrayMembers(line);

        for (int i = 0; i < numActivities; i++) {
            int duration = Integer.parseInt(durationPerActivityArray[i]);
            durationPerActivity[i] = duration;
        }
        return durationPerActivity;
    }

    private HashMap<Integer, Set<Integer>> readPredecessors() throws IOException {
        int numRelations = readValue("nPrecs");
        skipTo("pred");

        HashMap<Integer, Set<Integer>> predecessors = new HashMap<>();

        String[] predArray = getArrayMembers(line);
        skipTo("succ");
        String[] succArray = getArrayMembers(line);

        for (int i = 0; i < numRelations; ++i) {
            int succ = Integer.parseInt(succArray[i]);
            if (!predecessors.containsKey(succ)) {
                predecessors.put(succ, new HashSet<>());
            }
            predecessors.get(succ).add(Integer.parseInt(predArray[i]));
        }

        return predecessors;
    }

    private boolean[] readSuccessors(HashMap<Integer, Set<Integer>> predecessors, int numActivities) {
        boolean[] successors = new boolean[numActivities + 1];
        for (int i = 0; i < numActivities; i++) {
            successors[i] = false;
        }
        for (int i = 0; i < numActivities; ++i) {
            Set<Integer> havePredecessors = predecessors.get(i);
            if (havePredecessors != null) {
                for (int pred : havePredecessors) {
                    successors[pred] = true;
                }
            }
        }
        return successors;
    }

    private String[] getArrayMembers(String line) {
        return line.substring(line.indexOf('[') + 1, line.indexOf(']')).split(",");
    }

    private Resource[] readResources(int numResources, int numSkills) throws IOException {
        Resource[] resources = new Resource[numResources];
        Skill[][] skillsPerResource = readSkills(numResources, numSkills);
        for (int i = 0; i < numResources; i++) {
            resources[i] = new Resource(i, skillsPerResource[i]);
        }
        return resources;
    }

    private Skill[][] readSkills(int numResources, int numSkills) throws IOException {
        String masteryArray = readMultilineArray("mastery");

        String[] skillsPerResourceArray = masteryArray.substring(masteryArray.indexOf('|') + 2,
                masteryArray.lastIndexOf('|'))
                .replace("\t", "")
                .split("\\|");

        Skill[][] skillsPerResource = new Skill[numResources][numSkills];

        for (int i = 0; i < numResources; i++) {
            String[] skillTypes = skillsPerResourceArray[i].split(",");
            Skill[] skills = new Skill[4];
            for (int j = 0; j < numSkills; ++j) {
                if (skillTypes[j].trim().equals("true")) {
                    skills[j] = new Skill(j);
                }
            }
            skillsPerResource[i] = skills;
        }
        return skillsPerResource;
    }

    protected void closeReader(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            LOGGER.log(Level.FINE, e.toString());
        }
    }

    /**
     * Getters and setters.
     */
    public int getMinMakespan() {
        return minMakespan;
    }

    public int getMaxMakespan() {
        return maxMakespan;
    }

    public int getNumActivities() {
        return numActivities;
    }

    public int getNumSkills() {
        return numSkills;
    }

    public int getNumResources() {
        return numResources;
    }

    public Resource[] getResources() {
        return resources;
    }

    public Activity[] getActivities() {
        return activities;
    }

    public HashMap<Integer, Set<Integer>> getPredecessors() {
        return predecessors;
    }
}
