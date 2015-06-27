/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.util.ArrayList;

/**
 *
 * @author fazo
 */
public class World {

    private int width, height;
    public ArrayList<Element> elements;
    public ArrayList<Element> graveyard;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        elements = new ArrayList();
        graveyard = new ArrayList();
    }

    public void update() {
        while (elements.size() < 20) {
            if (Math.random() < 0.2) {
                spawnCreature();
            } else {
                spawnVegetable();
            }
            /*Creature c = new Creature(300,400);
             elements.add(c);
             elements.add(new Vegetable(300,450));*/
        }
        elements.removeAll(graveyard);
        graveyard.clear();
        for(Element e: elements) e.update();
    }

    private void spawn(boolean isCreature) {
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
            System.out.println("New Creat: " + x + " " + y);
            elements.add(new Creature(x, y));
        } else {
            System.out.println("New Veg: " + x + " " + y);
            elements.add(new Vegetable(x, y));
        }
    }

    private void spawnVegetable() {
        spawn(false);
    }

    private void spawnCreature() {
        spawn(true);
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

    public ArrayList<Element> getGraveyard() {
        return graveyard;
    }
    
}
