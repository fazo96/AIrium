package logic.creatures;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.Element;
import logic.Vegetable;
import logic.neural.Brain;

/**
 * A (hopefully) smart biological creature in the simulated world.
 *
 * @author fazo
 */
public class Creature extends Element implements Runnable {

    public static int brain_hidden_layers = 2, brain_hidden_neurons = 10;
    public static float corpseDecayRate = 0, pointsForEatingPlants = 1f, pointsForAttacking = 2f, hpForAttacking = 1f, hpForEatingPlants = 1f;
    public static boolean leaveCorpses = false;

    private final Brain brain;
    private final Torso torso;
    private final Beak beak;
    private final ArrayList<BodyPart> bodyParts;
    private float dir, fitness = 0;
    private boolean workerDone = false, killWorker = false;
    private Sight[] sights;
    private Thread workerThread;

    /**
     * Create a creature with a random mind at given position in space
     *
     * @param x
     * @param y
     */
    public Creature(float x, float y) {
        super(x, y, Torso.default_radius);
        dir = (float) (Math.random() * 2 * Math.PI);
        bodyParts = new ArrayList<BodyPart>();
        bodyParts.add(torso = new Torso(this));
        bodyParts.add(beak = new Beak(0, this));
        bodyParts.add(new Eye(5, 0, this));
        bodyParts.add(new Movement(this));
        brain = new Brain(howManyInputNeurons(), howManyOutputNeurons(), brain_hidden_layers, brain_hidden_neurons);
        sights = new Sight[2];
    }

    @Override
    public void run() {
        for (;;) {
            if (workerDone) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            } else {
                update();
                workerDone = true;
            }
            if (killWorker) {
                break;
            }
        }
    }

    @Override
    public void update() {
        if (!torso.isAlive()) { // Dead
            Game.get().getWorld().getGraveyard().add(this);
            if (leaveCorpses) {
                Vegetable carcass = new Vegetable(getX(), getY());
                carcass.setSize(getSize());
                carcass.setDecayRate(corpseDecayRate);
                Game.get().getWorld().add(carcass);
            }
            killWorker = true;
            return;
        }
        // collect inputs
        float values[] = new float[howManyInputNeurons()];
        int i = 0;
        for (BodyPart b : bodyParts) {
            for (float v : b.act()) {
                values[i] = v;
                i++;
            }
        }
        // read from sensors and interact with world
        interactWithWorld();
        // compute behavior
        float[] actions = null;
        try {
            actions = brain.compute(values);
        } catch (Exception ex) {
            // Should not happen
            Logger.getLogger(Creature.class.getName()).log(Level.SEVERE, null, ex);
        }
        i = 0;
        // Save brain outputs to body parts
        for (BodyPart b : bodyParts) {
            int n = 0;
            float data[] = new float[b.getOutputNeuronsUsed()];
            while (n < b.getOutputNeuronsUsed()) {
                data[n] = actions[i];
                i++;
                n++;
            }
            b.readFromBrain(data);
        }
    }

    @Override
    public void render(ShapeRenderer s) {
        // Draw Body
        for (BodyPart b : bodyParts) {
            b.render(s);
        }
    }

    /**
     * Make the body components interact with the world
     */
    public void interactWithWorld() {
        for (Element e : Game.get().getWorld().getElements()) {
            float distance = distanceFrom(e);
            float angle = (float) (Math.atan2(getY() - e.getY(), getX() - e.getX())) - (dir - (float) Math.PI);
            for (BodyPart b : bodyParts) {
                b.interactWithElement(e, distance, angle);
            }
        }
    }

    public final int howManyInputNeurons() {
        int n = 0;
        for (BodyPart b : bodyParts) {
            n += b.getInputNeuronsUsed();
        }
        return n;
    }

    public final int howManyOutputNeurons() {
        int n = 0;
        for (BodyPart b : bodyParts) {
            n += b.getOutputNeuronsUsed();
        }
        return n;
    }

    public void rotate(float amount) {
        dir += amount;
        if (dir > 2 * Math.PI) {
            dir -= 2 * Math.PI;
        } else if (dir < 0) {
            dir += 2 * Math.PI;
        }
    }

    /**
     * Praise this creature by increasing fitness. Can be negative to decrease
     * fitness
     *
     * @param amount how much
     */
    public void praise(float amount) {
        fitness += amount;
    }

    /**
     * Check if the Worker thread has finished its current iteration
     *
     * @return true if worker thread has finished its current iteration
     */
    public boolean isWorkerDone() {
        return workerDone;
    }

    /**
     * Command the Worker thread to start another iteration.
     */
    public void startWorker() {
        workerDone = false;
        if (workerThread == null) {
            // Create a new thread
            workerThread = new Thread(this);
            workerThread.start();
        } else {
            // Interrupt current thread, throwing it out of sleep
            workerThread.interrupt();
        }
    }

    public Brain getBrain() {
        return brain;
    }

    public void setDirection(float dir) {
        this.dir = dir;
    }

    public float getDirection() {
        return dir;
    }

    public float getFitness() {
        return fitness;
    }

    public float getBeak() {
        return beak.getLength();
    }

    public Torso getTorso() {
        return torso;
    }

    public ArrayList<BodyPart> getBodyParts() {
        return bodyParts;
    }

}
