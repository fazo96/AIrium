/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import com.mygdx.game.Game;
import com.mygdx.game.Listener;
import com.mygdx.game.Log;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fazo
 */
public class World implements Runnable {

    private int width, height, nPlants, creatPerGen;
    private int generation = 1;
    private boolean multithreading, cmdLaunchNewGen = false, cmdRestart = false;
    private int fpsLimit, fps = 0;
    private Map<String, Float> options;
    private long ticksSinceGenStart = 0, maximumTicksPerGen = 0;
    private Creature selected;
    private Comparator creatureComp;
    private final ArrayList<Element> elements;
    private final ArrayList<Element> toAdd;
    private final ArrayList<Creature> creatures;
    private final ArrayList<Creature> graveyard;
    private final ArrayList<Vegetable> plants;
    private final ArrayList<Vegetable> deadPlants;
    private final ArrayList<Listener> listeners;

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
        for (Element e : toAdd) {
            elements.add(e);
            if (e instanceof Creature) {
                creatures.add((Creature) e);
            } else if (e instanceof Vegetable) {
                plants.add((Vegetable) e);
            }
        }
        toAdd.clear();
        elements.removeAll(graveyard);
        if (selected != null && graveyard.contains(selected)) {
            selected = null;
            Log.log(Log.INFO, "Cleared selection");
        }
        elements.removeAll(deadPlants);
        plants.removeAll(deadPlants);
        deadPlants.clear();
        if (creatures.removeAll(graveyard)) {
            fire(Listener.CREATURE_LIST_CHANGED);
        }
        if (cmdLaunchNewGen) {
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
        if (multithreading) { // Multi-thread: use workers
            for (Vegetable v : plants) {
                v.update();
            }
            for (Creature c : creatures) {
                c.startWorker();
            }
            Thread.yield();
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
                    Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
                }
                Creature ne = spawnCreature(n);
                ne.getBrain().mutate(0.05f); // mutate children
            }
            graveyard.clear();
            fire(Listener.CREATURE_LIST_CHANGED);
            generation++;
        }
    }

    public void reloadOptions() {
        width = Math.round(options.getOrDefault("world_width", 2000f));
        height = Math.round(options.getOrDefault("world_height", 2000f));
        fpsLimit = Math.round(options.getOrDefault("fps_limit", 60f));
        maximumTicksPerGen = Math.round(options.getOrDefault("max_ticks", 0f));
        creatPerGen = Math.round(options.getOrDefault("number_of_creatures", (float) Math.min(Math.round(width * height / 20000), 50)));
        nPlants = Math.round(options.getOrDefault("number_of_plants", width * height / 5500f));
        multithreading = options.getOrDefault("enable_multithreading", -1f) > 0;
        Creature.corpseDecayRate = options.getOrDefault("corpse_decay_rate", 0f);
        Creature.leaveCorpses = options.getOrDefault("enable_corpses", 0f) > 0;
        Creature.default_radius = Math.round(options.getOrDefault("creature_radius", 20f));
        Creature.max_hp = Math.round(options.getOrDefault("creature_max_hp", 100f));
        Creature.max_speed = Math.round(options.getOrDefault("creature_max_speed", 3f));
        Creature.fov = Math.round(options.getOrDefault("creature_fov", (float) Math.PI / 2.5f));
        Creature.sightRange = Math.round(options.getOrDefault("creature_sight_range", 100f));
        Creature.hpDecay = options.getOrDefault("creature_hp_decay", 0.5f);
    }

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

    public void selectCreatureAt(int x, int y) {
        selected = null; // Clear selection
        try {
            for (Creature c : creatures) {
                if (c.overlaps(x, y)) {
                    selected = c;
                    Log.log(Log.INFO, "Selected a creature");
                }
            }
        } catch (ConcurrentModificationException ex) {
        }
    }

    public void fire(int eventCode) {
        for (Listener f : listeners) {
            f.on(eventCode);
        }
    }

    private void spawnVegetable() {
        spawn(false, null);
    }

    private Creature spawnCreature() {
        return (Creature) spawn(true, null);
    }

    private Creature spawnCreature(float[][][] b) {
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

    public void restart() {
        cmdRestart = true;
    }

    public void launchNewGen() {
        cmdLaunchNewGen = true;
    }
}
