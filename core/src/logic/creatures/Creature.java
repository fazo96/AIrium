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
 * A (hopefully) smart biological creature in the simulated world. It is
 * initialized with a set of body parts. Every creature has a brain which gets
 * automatically wired to the body parts. Only creatures with matching brain
 * structures can breed for now.
 *
 * @author fazo
 */
public abstract class Creature extends Element implements Runnable {

    public static int brain_hidden_layers = 2, brain_hidden_neurons = 10;
    public static double corpseDecayRate = 0, pointsForEatingPlants = 1f, pointsForAttacking = 2f, hpForAttacking = 1f, hpForEatingPlants = 1f;
    public static boolean leaveCorpses = false;

    private final Brain brain;
    private final Torso torso;
    private final ArrayList<BodyPart> bodyParts;
    private double dir, fitness = 0;
    private boolean workerDone = false, killWorker = false;
    private Thread workerThread;

    /**
     * Create a creature with a random mind at given position in space
     *
     * @param x
     * @param y
     */
    public Creature(double x, double y) {
        super(x, y, Torso.default_radius);
        dir = (double) (Math.random() * 2 * Math.PI);
        bodyParts = new ArrayList<BodyPart>();
        bodyParts.add(torso = new Torso(this));
        buildBody();
        brain = new Brain(howManyInputNeurons(), howManyOutputNeurons(), brain_hidden_layers, brain_hidden_neurons);
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
        double values[] = new double[howManyInputNeurons()];
        int i = 0;
        for (BodyPart b : bodyParts) {
            for (double v : b.act()) {
                values[i] = v;
                i++;
            }
        }
        // read from sensors and interact with world
        interactWithWorld();
        // compute behavior
        double[] actions = null;
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
            double data[] = new double[b.getOutputNeuronsUsed()];
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
            double distance = distanceFrom(e);
            double angle = (double) (Math.atan2(getY() - e.getY(), getX() - e.getX())) - (dir - (double) Math.PI);
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

    public void rotate(double amount) {
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
    public void praise(double amount) {
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

    public double getDangerLevel() {
        int beaks = 0;
        double danger = 0;
        for (BodyPart b : bodyParts) {
            if (b instanceof Beak) {
                beaks++;
                danger += (((Beak) b).getLength() - Beak.min_length) / Beak.max_length;
            }
        }
        if (beaks == 0) {
            return 0;
        }
        return danger / beaks;
    }

    /**
     * Compose this creature's body using the addBodyPart function.
     */
    public abstract void buildBody();

    public void addBodyPart(BodyPart p) {
        bodyParts.add(p);
    }

    public Brain getBrain() {
        return brain;
    }

    public void setDirection(double dir) {
        this.dir = dir;
    }

    public double getDirection() {
        return dir;
    }

    public double getFitness() {
        return fitness;
    }

    public Torso getTorso() {
        return torso;
    }

    public ArrayList<BodyPart> getBodyParts() {
        return bodyParts;
    }

}
