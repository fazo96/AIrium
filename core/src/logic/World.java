/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import com.mygdx.game.Log;
import java.util.ArrayList;
import java.util.Comparator;
import logic.neural.Brain;

/**
 *
 * @author fazo
 */
public class World {

    public static final int creatPerGen = 10;
    private int width, height;
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
        newGen();
    }

    public void update() {
        elements.removeAll(graveyard);
        elements.removeAll(deadPlants);
        plants.removeAll(deadPlants);
        creatures.removeAll(graveyard);
        deadPlants.clear();
        if (creatures.isEmpty()) {
            // All dead, next gen
            newGen();
        }
        while (plants.size() < 50) {
            spawnVegetable();
        }
        for (Creature e : creatures) {
            e.update();
        }
    }

    public void newGen() {
        elements.removeAll(creatures);
        creatures.clear();
        Comparator creatureComp = new Comparator<Creature>() {

            @Override
            public int compare(Creature t, Creature t1) {
                if (t.getFitness() < t1.getFitness()) {
                    return -1;
                } else if (t.getFitness() > t1.getFitness()) {
                    return 1;
                }
                return 0;
            }
        };
        if (graveyard.size() == 0) { // First gen
            for (int i = 0; i < creatPerGen; i++) {
                spawnCreature();
            }
        } else { // Mutate previous gen
            //graveyard.sort(creatureComp);
            int x = 0;
            for (Creature c : graveyard) {
                c.getBrain().remap(c.getBrain().getMutatedMap(3f));
                if (x < creatPerGen) {
                    c.setHp(100);
                    creatures.add(c);
                    elements.add(c);
                } else {
                    break;
                }
            }
            graveyard.clear();
        }
    }

    private void spawn(boolean isCreature, Brain brain) {
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
            Log.log(Log.INFO,"New Creat: " + x + " " + y);
            Creature c = new Creature(x, y);
            elements.add(c);
            creatures.add(c);
        } else {
            Log.log(Log.INFO,"New Veg: " + x + " " + y);
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

    private void spawnCreature(Brain b) {
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
