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
import logic.neural.Brain;

/**
 *
 * @author fazo
 */
public class World {

    public static final int creatPerGen = 10;
    private int width, height, generation = 0;
    public ArrayList<Element> elements;
    public ArrayList<Creature> creatures;
    public ArrayList<Creature> graveyard;
    public ArrayList<Vegetable> plants;
    public ArrayList<Vegetable> deadPlants;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        elements = new ArrayList();
        creatures = new ArrayList();
        plants = new ArrayList();
        deadPlants = new ArrayList();
        graveyard = new ArrayList();
        newGen(true);
    }

    public void update() {
        elements.removeAll(graveyard);
        elements.removeAll(deadPlants);
        plants.removeAll(deadPlants);
        creatures.removeAll(graveyard);
        deadPlants.clear();
        if (creatures.isEmpty()) {
            // All dead, next gen
            newGen(false);
        }
        while (plants.size() < 50) {
            spawnVegetable();
        }
        for (Creature e : creatures) {
            e.update();
        }
    }

    public void newGen(boolean restart) {
        elements.removeAll(creatures);
        graveyard.addAll(creatures);
        Comparator creatureComp = new Comparator<Creature>() {

            @Override
            public int compare(Creature t, Creature t1) {
                // put the highest fitness first (sort in reverse)
                return (int) (t1.getFitness() - t.getFitness() );
                /*if (t.getFitness() < t1.getFitness()) {
                 return -1;
                 } else if (t.getFitness() > t1.getFitness()) {
                 return 1;
                 }
                 return 0;*/
            }
        };
        if (graveyard.isEmpty() || restart) { // First gen
            generation = 0;
            for (int i = 0; i < creatPerGen; i++) {
                spawnCreature();
            }
        } else { // Evolve previous gen
            // Calculate avg fitness
            float avgFitness = 0;
            for (Creature c : graveyard) {
                avgFitness += c.getFitness();
            }
            avgFitness = avgFitness / graveyard.size();
            Log.log(Log.INFO, "Gen " + generation + " done. Avg fitness: " + avgFitness);
            // Start evolution
            graveyard.sort(creatureComp);
            for (int i = 0; i < creatPerGen / 2; i++) {
                Creature c = graveyard.get(i);
                c.reset();
                // Mutate
                if (i != 0) {
                    try {
                        // create a child
                        float[][][] mind = c.getBrain().breed(graveyard.get(i - 1).getBrain().getMap());
                        // spawn it
                        spawnCreature(mind);
                    } catch (Exception ex) {
                        // Should never happen
                        Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // Mutate parent
                c.getBrain().remap(c.getBrain().getMutatedMap(0.1f));
                // Add it back in
                creatures.add(c);
                elements.add(c);
            }
            graveyard.clear();
            generation++;
        }
    }

    private void spawn(boolean isCreature, float[][][] brainMap) {
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
            Log.log(Log.INFO, "New Creat: " + x + " " + y);
            Creature c = new Creature(x, y);
            if (brainMap != null) {
                c.getBrain().remap(brainMap);
            }
            elements.add(c);
            creatures.add(c);
        } else {
            Log.log(Log.INFO, "New Veg: " + x + " " + y);
            Vegetable v = new Vegetable(x, y);
            elements.add(v);
            plants.add(v);
        }
    }

    private void spawnVegetable() {
        spawn(false, null);
    }

    private void spawnCreature() {
        spawn(true, null);
    }

    private void spawnCreature(float[][][] b) {
        spawn(true, b);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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

}
