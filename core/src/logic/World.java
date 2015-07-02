/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import com.mygdx.game.Log;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fazo
 */
public class World {

    private final int width, height, nPlants, creatPerGen;
    private int generation = 1;
    public ArrayList<Element> elements;
    public ArrayList<Element> toAdd;
    public ArrayList<Creature> creatures;
    public ArrayList<Creature> graveyard;
    public ArrayList<Vegetable> plants;
    public ArrayList<Vegetable> deadPlants;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        elements = new ArrayList();
        creatures = new ArrayList();
        toAdd = new ArrayList();
        creatPerGen = 50;
        nPlants = Math.round(width * height / 5500);
        plants = new ArrayList();
        deadPlants = new ArrayList();
        graveyard = new ArrayList();
        newGen(true);
    }

    public void update() {
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
        elements.removeAll(deadPlants);
        plants.removeAll(deadPlants);
        deadPlants.clear();
        creatures.removeAll(graveyard);
        if (creatures.isEmpty()) {
            // All dead, next gen
            newGen(false);
        }
        while (plants.size() < nPlants) {
            spawnVegetable();
        }
        for (Element e : elements) {
            e.update();
        }
    }

    public void newGen(boolean restart) {
        elements.removeAll(creatures);
        graveyard.addAll(creatures);
        creatures.clear();
        Comparator creatureComp = new Comparator<Creature>() {

            @Override
            public int compare(Creature t, Creature t1) {
                // put the highest fitness first (sort in reverse)
                return (int) (t1.getFitness() - t.getFitness());
            }
        };
        if (graveyard.isEmpty() || restart) { // First gen
            generation = 1;
            Log.log(Log.INFO, "Starting from generation 1: spawning "+creatPerGen+" creatures.");
            for (int i = 0; i < creatPerGen; i++) {
                spawnCreature();
            }
        } else { // Evolve previous gen
            graveyard.sort(creatureComp); // sort by fitness
            // Prepare best agent list
            int topSize = (int) Math.round(graveyard.size() * 0.05f);
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
            Log.log(Log.INFO, "Gen " + generation + " done. Avg fitness: " + avgFitness);
            // Generate children
            for (Creature c : graveyard) {
                int first = (int) Math.floor(Math.random() * topSize);
                int sec = first;
                while (sec == first) {
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
            generation++;
        }
    }

    private Element spawn(boolean isCreature, float[][][] brainMap) {
        int x, y, r;
        boolean overlaps = false;
        if (isCreature) {
            r = Creature.default_radius;
        } else {
            r = Vegetable.default_radius;
        }
        do {
            overlaps = false;
            x = (int) (Math.random() * width);
            y = (int) (Math.random() * height);
            for (Element e : elements) {
                if (e.overlaps(x, y, r)) {
                    overlaps = true;
                }
            }
        } while (overlaps);
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

}
