package logic;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game;
import com.mygdx.game.Log;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.neural.Brain;

/**
 * A (hopefully) smart biological creature in the simulated world.
 *
 * @author fazo
 */
public class Creature extends Element implements Runnable {

    public static int default_radius = 20, max_hp = 100;
    public static float max_speed = 3, max_beak = default_radius / 4, fov, sightRange, corpseDecayRate = 0, hpDecay = 0.5f, pointsForEatingPlants = 1f, pointsForAttacking = 2f, hpForAttacking = 1f, hpForEatingPlants = 1f;
    public static boolean leaveCorpses = false;

    private Brain brain;
    private float dir, hp, prevHp, speed, fitness, rotSpeed, beak;
    private boolean eating = false, killing = false, workerDone = false, killWorker = false;
    private Sight[] sights;
    private Thread workerThread;

    /**
     * Create a creature with a random mind at given position in space
     *
     * @param x
     * @param y
     */
    public Creature(float x, float y) {
        super(x, y, default_radius);
        dir = (float) (Math.random() * 2 * Math.PI);
        hp = max_hp;
        prevHp = hp;
        speed = 0;
        rotSpeed = 0;
        fitness = 0;
        brain = new Brain(9, 5, 2, 10);
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
            if(killWorker) break;
        }
    }

    @Override
    public void update() {
        // apply hunger
        hp -= hpDecay;
        prevHp = hp;
        if (hp < 0) { // Dead
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
        if (speed > max_speed) {
            speed = max_speed;
        }
        if (speed < -max_speed) {
            speed = -max_speed;
        }
        // apply speed
        float xMul = (float) Math.cos(dir), yMul = (float) Math.sin(dir);
        move(xMul * speed, yMul * speed);
        if (getX() < 0) {
            setX(Game.get().getWorld().getWidth() + getX());
        }
        if (getY() < 0) {
            setY(Game.get().getWorld().getHeight() + getY());
        }
        if (getX() > Game.get().getWorld().getWidth()) {
            setX(getX() - Game.get().getWorld().getWidth());
        }
        if (getY() > Game.get().getWorld().getHeight()) {
            setY(getY() - Game.get().getWorld().getHeight());
        }
        dir += rotSpeed;
        //fitness -= 0.1;
        if (dir > 2 * Math.PI) {
            dir -= 2 * Math.PI;
        }
        if (dir < 0) {
            dir += 2 * Math.PI;
        }
        // read from sensors and interact with world
        sights = interactWithWorld();
        // feed data to brain
        float[] values = new float[brain.getNeurons()[0].length];
        // VIEW: PLANTS
        // 0: sight(v): see food?
        // 1: sight(v): distance
        // 2: sight(v): angle
        // 3: sight(v): size
        // VIEW: CREATS
        // 4: sight(c): see food?
        // 5: sight(c): distance
        // 6: sight(c): angle
        // 8: sight(c): hunger
        // 7: sight(c): beak
        // OTHER:
        // 8: food sensor
        int viewSensors = 4;
        for (int i = 0; i < sights.length; i++) {
            int mul = i * viewSensors;
            if (sights[i] == null || sights[i].getElement() == null) {
                // See nothing
                values[0 + mul] = 0;
                values[1 + mul] = 0;
                values[2 + mul] = 0;
                values[3 + mul] = 0;
            } else {
                // See something
                values[1 + mul] = sights[i].getDistance() / sightRange;
                values[2 + mul] = sights[i].getAngle();
                if (sights[i].getElement() instanceof Vegetable) {
                    values[0 + mul] = 1f;
                    values[3 + mul] = sights[i].getElement().getSize() / default_radius;
                } else {
                    values[0 + mul] = 1f;
                    values[3 + mul] = max_hp - ((Creature) sights[i].getElement()).getHp() / max_hp;
                    values[3 + mul] = ((Creature) sights[i].getElement()).getBeak() / max_beak;
                }
            }
        }
        values[8] = eating || killing ? 1 : 0;
        // compute behavior
        float[] actions = null;
        try {
            actions = brain.compute(values);
        } catch (Exception ex) {
            // Should not happen
            Logger.getLogger(Creature.class.getName()).log(Level.SEVERE, null, ex);
        }
        Log.log(Log.DEBUG, "Accel: " + actions[0] + " RotClock: " + actions[1] + " RotAntiClock: " + actions[2] + " Beak: " + actions[3]);
        speed = (actions[0] * 2 - actions[4] / 2) * max_speed;
        rotSpeed = actions[1] - actions[2];
        beak = actions[3] * max_beak;
        if (beak > max_beak) {
            beak = max_beak;
        } else if (beak < 0) {
            beak = 0;
        }
    }

    @Override
    public void render(ShapeRenderer s) {
        // Draw Body
        s.setColor(1 - (hp / max_hp), hp / max_hp, 0, 1);
        s.circle(getX(), getY(), getSize());
        // Prepare vision stuff
        double relX = Math.cos(dir), relY = Math.sin(dir);
        float c = 0;
        float eyeX = (float) (relX * getSize() * 0.6f), eyeY = (float) (relY * getSize() * 0.6f);
        // Draw Sight Lines
        if (Game.get().getWorld().getOptions().getOrDefault("draw_sight_lines", 0f) > 0) {
            for (Sight sight : sights) {
                if (sight != null) {
                    c = sight.getDistance() / sightRange * 2 + sightRange;
                } else {
                }
                if (sight != null) {
                    if (sight.getElement() instanceof Creature) {
                        s.setColor(c, 0, 0, 1);
                    } else if (sight.getElement() instanceof Vegetable) {
                        s.setColor(0, c, 0, 1);
                    }
                    s.line(eyeX + getX(), getY() + eyeY, sight.getElement().getX(), sight.getElement().getY());
                }
            }
        }
        // Draw eye
        if (sights[0] == null && sights[1] == null) {
            s.setColor(1, 1, 1, 1);
        } else {
            s.setColor(sights[1] == null ? 0 : 1, sights[0] == null ? 0 : 1, 0, 1);
        }
        s.circle(getX() + eyeX, getY() + eyeY, 3);
        // Draw FOV cone
        float degrees = fov * 360f / (float) Math.PI;
        float orient = dir * 180f / (float) Math.PI - degrees / 2;
        if (Game.get().getWorld().getOptions().getOrDefault("draw_view_cones", 0f) > 0) {
            s.setColor(0.3f, 0.3f, 0.3f, 1);
            s.arc((float) eyeX + getX(), (float) eyeY + getY(), sightRange, orient, degrees);
        }
        // Draw damage/heal marks
        if (hp < prevHp) {
            // Damage mark
            s.set(ShapeRenderer.ShapeType.Filled);
            s.setColor(1, 0, 0, 1);
            s.circle(getX(), getY(), 5);
        } else if (killing || eating) {
            // Heal mark
            s.set(ShapeRenderer.ShapeType.Filled);
            s.setColor(0, 1, 0, 1);
            s.circle(getX(), getY(), 5);
        }
        s.set(ShapeRenderer.ShapeType.Line);
        // Draw Beak
        s.setColor(beak / max_beak, 1 - beak / max_beak, 0, 1);
        s.line((float) (relX * getSize() * 0.8f + getX()), (float) (relY * getSize() * 0.8f + getY()), (float) (relX * getSize() * (1.5f + beak / max_beak) + getX()), (float) (relY * getSize() * (1.5f + beak / max_beak) + getY()));
    }

    /**
     * Store Sight information (what the creature sees) and eat/attack if
     * applicable
     *
     * @return the sight information retrieved
     */
    public Sight[] interactWithWorld() {
        Sight[] newSights = new Sight[2];
        // Try to see plant
        Element seen = null;
        float dist = 0, angle = 0, ndir = dir - (float) Math.PI;
        eating = false;
        for (Element e : Game.get().getWorld().getPlants()) {
            float tempDist = distanceFrom(e);
            if (tempDist > sightRange) {
                continue;
            }
            //Log.log(Log.DEBUG,"TempDist "+tempDist+" SightRange "+sightRange);
            float relAngle = (float) (Math.atan2(getY() - e.getY(), getX() - e.getX()));
            if (tempDist < dist || seen == null) {
                // Check if Visible
                if (Math.abs(relAngle - ndir) < fov) {
                    // Visible
                    seen = e;
                    angle = relAngle - ndir;
                    dist = tempDist;
                }
                //Log.log(Log.DEBUG,"RelAngle "+relAngle+" Dir "+ndir);
            }
            // Check if eatable
            if (tempDist < 0) {
                // Eat
                eating = true;
                e.setSize(e.getSize() - 0.1f);
                if (e.getSize() == 0) {
                    e.setSize(0);
                }
                hp += hpForEatingPlants;
                fitness+=pointsForEatingPlants;
                if (hp > max_hp) {
                    hp = max_hp;
                }
            }
        }
        if (seen != null) {
            newSights[0] = new Sight(seen, dist, angle);
        }
        // Try to see creature
        seen = null;
        dist = 0;
        angle = 0;
        ndir = dir - (float) Math.PI;
        killing = false;
        for (Element e : Game.get().getWorld().getCreatures()) {
            if (e == this) {
                continue;
            }
            float tempDist = distanceFrom(e);
            if (tempDist > sightRange) {
                continue;
            }
            //Log.log(Log.DEBUG,"TempDist "+tempDist+" SightRange "+sightRange);
            float relAngle = (float) (Math.atan2(getY() - e.getY(), getX() - e.getX()));
            if (tempDist < dist || seen == null) {
                // Check if Visible
                float tempAngle = Math.abs(relAngle - ndir);
                if (tempAngle < fov) {
                    // Visible
                    seen = e;
                    angle = relAngle - ndir;
                    dist = tempDist;
                    // Check if attackable
                    if (beak > beak / 2 && tempDist < beak * 1.5f && tempAngle < fov / 2) {
                        // Attacking!
                        float damage = beak;
                        hp += damage * hpForAttacking / 2;
                        fitness += pointsForAttacking;
                        if (hp > max_hp) {
                            hp = max_hp;
                        }
                        killing = true;
                        Creature c = (Creature) e;
                        c.heal(-damage);
                        //c.praise(-1);
                    }
                }
                //Log.log(Log.DEBUG,"RelAngle "+relAngle+" Dir "+ndir);
            }
        }
        if (seen != null) {
            newSights[1] = new Sight(seen, dist, angle);
        }
        return newSights;
    }

    /**
     * Apply a modification to this creature's health. Can be negative.
     *
     * @param amount how much to heal/damage
     */
    private void heal(float amount) {
        hp += amount;
    }

    /**
     * Praise this creature by increasing fitness. Can be negative to decrease
     * fitness
     *
     * @param amount how much
     */
    private void praise(float amount) {
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

    public float getFitness() {
        return fitness;
    }

    public void reset() {
        fitness = 0;
        hp = max_hp;
    }

    public float getBeak() {
        return beak;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = hp;
    }
}
