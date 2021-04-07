package environments;

import helperclasses.Util;
import nl.uu.cs.aplib.utils.Pair;
import game.Platform;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This defines some configuration parameters for the Lab-Recruits game, as far
 * as they can be configured. This configuration will be sent to the Lab Recruits
 * when we connect iv4xr Environment to it.
 */
public class LabRecruitsConfig {

    // will not be sent over to Unity
    public transient String host = "localhost";
    public transient int port = 8053;

    // configurations
    public int seed = 1;

    public String level_path = "";
    public String level_name = "";

    public float agent_speed = 0.13f;
    public float npc_speed = 0.11f;
    public float fire_spread = 0f;
    public float jump_force = 0.18f;
    public float view_distance = 10f;
    public float light_intensity = 1f; // does not seem to work ... TODO

    /**
     *  Extra links between switches and doors that we want to explicitly
     *  add through this configuration.
     */
    public ArrayList<Pair<String, String>> add_links = new ArrayList<>();
    
    /**
     *  Links between switches and doors that we want to explicitly remove
     *  through this configuration.
     */
    public ArrayList<Pair<String, String>> remove_links = new ArrayList<>();

    /**
     * Return a default configuration. The path to the level-file and the name of the level
     * are left blank though.
     */
    public LabRecruitsConfig(){}
    
    /**
     * Create a default configuration, with the given level-name. The level-file is assumed
     * to be stored in the standard location defined by Platform.LEVEL_PATH (which points to
     * projectdir/src/test/resources/levels".
     */
    public LabRecruitsConfig(String levelName){
        useLevel(levelName, Platform.LEVEL_PATH);
    }

    /**
     * Create a default configuration, with the given level-name. The level-file is assumed
     * to be stored in the given folder-path.
     */ 
    public LabRecruitsConfig(String levelName, String levelFolder){
        useLevel(levelName, levelFolder);
    }
    private LabRecruitsConfig useLevel(String levelName, String levelFolder){
        String fullPath = Paths.get(levelFolder, levelName + ".csv").toAbsolutePath().toString();
        Util.verifyPath(fullPath);
        this.level_path = fullPath;
        this.level_name = levelName;
        return this;
    }

    public LabRecruitsConfig replaceSeed(int seed){
        this.seed = seed;
        return this;
    }

    public LabRecruitsConfig replaceNPCMovementSpeed(float arg){
        this.npc_speed = arg;
        return this;
    }

    public LabRecruitsConfig replaceAgentMovementSpeed(float arg){
        agent_speed = arg;
        return this;
    }

    public LabRecruitsConfig replaceJumpForce(float arg){
        this.jump_force = arg;
        return this;
    }

    public LabRecruitsConfig replaceAgentViewDistance(float arg){
        this.view_distance = arg;
        return this;
    }

    public LabRecruitsConfig replaceLightIntensity(float arg){
        this.light_intensity = arg;
        return this;
    }

    public LabRecruitsConfig replaceFireSpreadSpeed(float arg){
        this.fire_spread = arg;
        return this;
    }

    /**
     * Add explicit links between switches and doors. Each link is represented by
     * an instance of Pair (s,d). 
     */
    public LabRecruitsConfig addSwitchToDoorLinks(Pair<String, String> ... links){
        this.add_links.addAll(Arrays.asList(links));
        return this;
    }

    /**
     * Add these links between switches and doors to be removed (so, if a switch s is 
     * connected to a door d in the level definition, and it is to be removed, then they
     * will no longer be connected. 
     * Each link is represented by an instance of Pair (s,d). 
     */
    public LabRecruitsConfig removeSwitchToDoorLinks(Pair<String, String> ... links){
        this.remove_links.addAll(Arrays.asList(links));
        return this;
    }
   
}
