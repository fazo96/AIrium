package logic;

import com.mygdx.game.Game;
import com.mygdx.game.Listener;
import com.mygdx.game.Log;
import com.mygdx.game.Serializer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.neural.Brain;

/**
 * This class represents an instance of a simulation, its world and its
 * configuration
 *
 * @author fazo
 */
public class World implements Runnable {

    private int width, height, nPlants, creatPerGen;
    private float nMutatedBrains = 0.2f, nMutatedNeurons = 0.5f, nMutatedConnections = 0.5f, mutationFactor = 1f;
    private int generation = 1;
    private boolean multithreading, cmdLaunchNewGen = false, cmdRestart = false;
    private int fpsLimit, fps = 0;
    private Map<String, Float> options;
    private long ticksSinceGenStart = 0, maximumTicksPerGen = 0;
    private Creature selected;
    private final Comparator creatureComp;
    private final ArrayList<Element> elements;
    private final ArrayList<Element> toAdd;
    private final ArrayList<Creature> creatures;
    private final ArrayList<Creature> graveyard;
    private final ArrayList<Vegetable> plants;
    private final ArrayList<Vegetable> deadPlants;
    private final ArrayList<Listener> listeners;

    /**
     * Create a new World. Can be customized with given options.
     *
     * @param options customization options. Can be null. See the
     * "reloadOptions" function for possible options
     */
    public World(Map<String, Float> options) {
        if (options == null) {
            this.options = new HashMap<String, Float>();
        } else {
            this.options = options;
        }
        reloadOptions();
        elements = new ArrayList();
        creatures = new ArrayList();
        toAdd = new ArrayList();
        plants = new ArrayList();
        deadPlants = new ArrayList();
        graveyard = new ArrayList();
        listeners = new ArrayList();
        selected = null;
        creatureComp = new Comparator<Creature>() {

            @Override
            public int compare(Creature t, Creature t1) {
                // put the highest fitness first (sort in reverse)
                return (int) (t1.getFitness() - t.getFitness());
            }
        };
    }

    public void start() {
        newGen(true);
    }

    @Override
    public void run() {
        Date d, timekeeper = new Date();
        long time;
        int target, frames = 0;
        for (;;) {
            if (!Game.get().isPaused()) {
                d = new Date();
                update();
                frames++;
                Date now = new Date();
                if (now.getTime() - timekeeper.getTime() > 1000) {
                    fps = frames;
                    frames = 0;
                    fire(Listener.FPS_CHANGED);
                    timekeeper = new Date();
                }
                if (fpsLimit > 0) {
                    time = now.getTime() - d.getTime();
                    target = 1000 / fpsLimit;
                    if (time < target) {
                        try {
                            Thread.sleep((long) (target - time));
                        } catch (InterruptedException ex) {
                            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void update() {
        if (cmdRestart) {
            // Clear everything and start over
            elements.clear();
            graveyard.clear();
            creatures.clear();
            plants.clear();
            toAdd.clear();
            deadPlants.clear();
            newGen(true);
            cmdRestart = false;
        }
        ticksSinceGenStart++;
        if (maximumTicksPerGen > 0 && ticksSinceGenStart >= maximumTicksPerGen) {
            // Force new gen
            Log.log(Log.INFO, "Reached maximum generation time (" + maximumTicksPerGen + ")");
            newGen(false);
        }
        // Add stuff to the world
        for (Element e : toAdd) {
            elements.add(e);
            if (e instanceof Creature) {
                creatures.add((Creature) e);
            } else if (e instanceof Vegetable) {
                plants.add((Vegetable) e);
            }
        }
        toAdd.clear();
        // Clear creature from graveyard
        elements.removeAll(graveyard);
        if (selected != null && graveyard.contains(selected)) {
            selected = null;
            Log.log(Log.DEBUG, "Cleared selection");
        }
        elements.removeAll(deadPlants);
        plants.removeAll(deadPlants);
        deadPlants.clear();
        if (creatures.removeAll(graveyard)) {
            fire(Listener.CREATURE_LIST_CHANGED);
        }
        if (cmdLaunchNewGen) {
            // Skip to new generation
            newGen(false);
            cmdLaunchNewGen = false;
        }
        if (creatures.isEmpty()) {
            // All dead, next gen
            newGen(false);
        }
        while (plants.size() < nPlants) {
            spawnVegetable();
        }
        if (multithreading) {
            // Multi-thread: use workers
            for (Creature c : creatures) {
                c.startWorker();
            }
        }
        for (Vegetable v : plants) {
            v.update();
        }
        if (multithreading) {
            int finishedCount = 0;
            while (finishedCount < creatures.size()) {
                finishedCount = 0;
                for (Creature c : creatures) {
                    if (c.isWorkerDone()) {
                        finishedCount++;
                    }
                }
            }
        } else { // Single-thread
            for (Element e : elements) {
                e.update();
            }
        }
    }

    private void newGen(boolean restart) {
        elements.removeAll(creatures);
        graveyard.addAll(creatures);
        creatures.clear();
        if (selected != null) {
            selected = null;
            Log.log(Log.INFO, "Cleared selection");
        }
        if (graveyard.isEmpty() || restart) { // First gen
            generation = 1;
            Log.log(Log.INFO, "Starting from generation 1: spawning " + creatPerGen + " creatures.");
            for (int i = 0; i < creatPerGen; i++) {
                spawnCreature();
            }
        } else { // Evolve previous gen
            graveyard.sort(creatureComp); // sort by fitness
            // Prepare best agent list
            int topSize;
            if (graveyard.size() == 1) {
                topSize = 1;
            } else if (options.containsKey("parents_count") && options.get("parents_count") >= 1) {
                topSize = (int) Math.max(2, Math.round(options.get("parents_count")));
            } else {
                topSize = (int) Math.max(2, Math.round(graveyard.size() * 0.05f));
            }
            Creature[] top = new Creature[topSize];
            // Calculate avg fitness and prepare best agent list
            float avgFitness = 0;
            for (int i = 0; i < graveyard.size(); i++) {
                Creature c = graveyard.get(i);
                if (i < topSize) {
                    top[i] = graveyard.get(i);
                    Log.log(Log.INFO, "Gen " + generation + " Top " + (i + 1) + ": " + c.getFitness());
                }
                avgFitness += c.getFitness();
            }
            avgFitness = avgFitness / graveyard.size();
            Log.log(Log.INFO, "Gen " + generation + " done. Ticks: " + ticksSinceGenStart + ". Avg fitness: " + avgFitness);
            ticksSinceGenStart = 0;
            // Generate children
            for (Creature c : graveyard) {
                int first = (int) Math.floor(Math.random() * topSize);
                int sec = first;
                while (sec == first && topSize > 1) {
                    sec = (int) Math.floor(Math.random() * topSize);
                }
                float[][][] n = null;
                try {
                    n = top[first].getBrain().breed(top[sec].getBrain().getMap());
                } catch (Exception ex) {
                    // Should not happen
                    Log.log(Log.ERROR, "Could not breed: " + ex.getMessage()
                            + "\nIt is advised to restart the simulation after changing the brain's topology");
                    Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
                }
                Creature ne = spawnCreature(n);
                if (Math.random() <= nMutatedBrains) {
                    ne.getBrain().mutate(nMutatedNeurons, nMutatedConnections, mutationFactor);
                }
            }
            graveyard.clear();
            fire(Listener.CREATURE_LIST_CHANGED);
            generation++;
        }
    }

    public void resetDefaultOptions() {
        options.clear();
        reloadOptions();
    }

    /**
     * Applies current options. Uses default alternatives if options are not
     * provided
     */
    public void reloadOptions() {
        for (Object o : Serializer.getDefaultSettings().entrySet().toArray()) {
            Map.Entry<String, Float> e = (Map.Entry<String, Float>) o;
            options.putIfAbsent(e.getKey(), e.getValue());
        }
        width = Math.round(options.get("world_width"));
        height = Math.round(options.get("world_height"));
        fpsLimit = Math.round(options.get("fps_limit"));
        maximumTicksPerGen = Math.round(options.get("max_ticks"));
        creatPerGen = Math.round(options.get("number_of_creatures"));
        nPlants = Math.round(options.get("number_of_plants"));
        multithreading = options.get("enable_multithreading") > 0;
        Creature.corpseDecayRate = options.get("corpse_decay_rate");
        Creature.leaveCorpses = options.get("enable_corpses") > 0;
        Creature.default_radius = Math.round(options.get("creature_radius"));
        Creature.max_hp = Math.round(options.get("creature_max_hp"));
        Creature.max_speed = options.get("creature_max_speed");
        Creature.fov = options.get("creature_fov");
        Creature.sightRange = options.get("creature_sight_range");
        Creature.hpDecay = options.get("creature_hp_decay");
        Creature.hpForAttacking = options.get("creature_hp_for_attacking");
        Creature.hpForEatingPlants = options.get("creature_hp_for_eating_plants");
        Creature.pointsForAttacking = options.get("creature_points_for_attacking");
        Creature.pointsForEatingPlants = options.get("creature_points_for_eating_plants");
        Creature.brain_hidden_layers = Math.round(options.get("brain_hidden_layers"));
        Creature.brain_hidden_neurons = Math.round(options.get("brain_hidden_neurons"));
        Brain.bias = options.get("brain_bias");
        nMutatedBrains = options.get("nMutatedBrains");
        nMutatedNeurons = options.get("nMutatedNeurons");
        nMutatedConnections = options.get("nMutatedConnections");
        mutationFactor = options.get("mutationFactor");
    }

    /**
     * Spawn a new random element in the world, at a random position.
     *
     * @param isCreature true if you want to spawn a creature
     * @param brainMap the brain configuration. Used if spawning a creature. If
     * null, a random mind will be created
     * @return the spawned element
     */
    private Element spawn(boolean isCreature, float[][][] brainMap) {
        int x, y, r;
        boolean overlaps = false;
        if (isCreature) {
            r = Creature.default_radius;
        } else {
            r = Vegetable.default_radius;
        }
        int i = 0;
        do {
            overlaps = false;
            x = (int) (Math.random() * width);
            y = (int) (Math.random() * height);
            for (Element e : elements) {
                if (e.overlaps(x, y, r)) {
                    overlaps = true;
                }
            }
        } while (overlaps && i++ < 20);
        if (isCreature) {
            Log.log(Log.DEBUG, "New Creat: " + x + " " + y);
            Creature c = new Creature(x, y);
            if (brainMap != null) {
                c.getBrain().remap(brainMap);
            }
            //add(c);
            elements.add(c);
            creatures.add(c);
            return c;
        } else {
            Log.log(Log.DEBUG, "New Veg: " + x + " " + y);
            Vegetable v = new Vegetable(x, y);
            //add(v);
            elements.add(v);
            plants.add(v);
            return v;
        }
    }

    /**
     * Sets currently select creature to the first creature that overlaps given
     * coordinates
     *
     * @param x the x coordinate of the creature you want to select
     * @param y the x coordinate of the creature you want to select
     */
    public void selectCreatureAt(int x, int y) {
        //selected = null; // Clear selection
        try {
            for (Creature c : creatures) {
                if (c.overlaps(x, y)) {
                    selected = c;
                    Log.log(Log.INFO, "Selected a creature");
                }
            }
        } catch (ConcurrentModificationException ex) {
            Log.log(Log.DEBUG, "Failed creature click selection");
        }
    }

    /**
     * Fire an event
     *
     * @param eventCode the event code. Look at the Listener class for event
     * codes.
     */
    public void fire(int eventCode) {
        Log.log(Log.DEBUG, "Firing Event. Code: " + eventCode);
        for (Listener f : listeners) {
            f.on(eventCode);
        }
    }

    public void spawnVegetable() {
        spawn(false, null);
    }

    public Creature spawnCreature() {
        return (Creature) spawn(true, null);
    }

    public Creature spawnCreature(float[][][] b) {
        return (Creature) spawn(true, b);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getGeneration() {
        return generation;
    }

    public void addListener(Listener f) {
        listeners.add(f);
    }

    public void add(Element e) {
        toAdd.add(e);
    }

    public ArrayList<Element> getElements() {
        return elements;
    }

    public ArrayList<Creature> getGraveyard() {
        return graveyard;
    }

    public ArrayList<Vegetable> getDeadPlants() {
        return deadPlants;
    }

    public ArrayList<Creature> getCreatures() {
        return creatures;
    }

    public ArrayList<Vegetable> getPlants() {
        return plants;
    }

    public float getFpsLimit() {
        return fpsLimit;
    }

    public void setFpsLimit(int fpsLimit) {
        this.fpsLimit = fpsLimit;
    }

    public float getFps() {
        return fps;
    }

    public Map<String, Float> getOptions() {
        return options;
    }

    public Creature getSelectedCreature() {
        return selected;
    }

    public void selectCreature(Creature selected) {
        this.selected = selected;
    }

    public boolean isMultithreading() {
        return multithreading;
    }

    public void setMultithreading(boolean multithreading) {
        this.multithreading = multithreading;
    }

    public void replaceOptions(Map<String, Float> options) {
        this.options = options;
        reloadOptions();
    }

    public void restart() {
        cmdRestart = true;
    }

    public void launchNewGen() {
        cmdLaunchNewGen = true;
    }
}
