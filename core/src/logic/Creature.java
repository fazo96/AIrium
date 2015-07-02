package logic;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game;
import com.mygdx.game.Log;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.neural.Brain;

/**
 * A (hopefully) smart biological creature.
 *
 * @author fazo
 */
public class Creature extends Element implements Runnable {

    public static final int default_radius = 20;
    public static final float max_speed = 3, max_rot_speed = 0.05f;

    private Brain brain;
    private float dir, hp, prevHp, speed, sightRange, fov, fitness, rotSpeed, beak;
    private boolean eating = false, killing = false, done = false;
    private Sight[] sights;
    private Thread thread;

    public Creature(float x, float y) {
        super(x, y, default_radius);
        dir = (float) (Math.random() * 2 * Math.PI);
        hp = 100;
        prevHp = 100;
        speed = 0;
        rotSpeed = 0;
        sightRange = 350;
        fov = (float) Math.PI / 2f;
        fitness = 0;
        brain = new Brain(10, 5, 2, 10);
        thread = new Thread(this);
    }

    @Override
    public void run() {
        for (;;) {
            done = false;
            while (Game.get().getWorld().isBusy()) {
                Thread.yield();
            }
            if (!update()) {
                break;
            }
            done = true;
        }
    }

    @Override
    public boolean update() {
        // apply hunger
        hp -= 0.3f;
        prevHp = hp;
        if (hp < 0) { // Dead
            Game.get().getWorld().getGraveyard().add(this);
            /*Vegetable carcass = new Vegetable(getX(), getY());
             carcass.setSize(getSize());
             carcass.setDecayRate(0.01f);
             Game.get().getWorld().add(carcass);*/
            return false;
        }
        // take a look
        sights = interactWithWorld();
        // FEED DATA TO BRAIN
        // Input Neurons:
        // 0: sight: see food?
        // 1: sight(veg): distance
        // 2: sight(veg): angle
        // 3: sight(veg): size
        // 4: sight(creat): see creat?
        // 5: sight(creat): distance
        // 6: sight(creat): angle
        // 7: sight(creat): hunger
        // 8: food sensor
        // 9: pain sensor
        float[] values = new float[brain.getNeurons()[0].length];
        for (int i = 0; i < 2; i++) {
            if (sights[i] == null || sights[i].getElement() == null) {
                // See nothing
                values[0 + 4 * i] = 0;
                values[1 + 4 * i] = 0;
                values[2 + 4 * i] = 0;
                values[3 + 4 * i] = 0;
                values[4 + 4 * i] = 0;
            } else {
                // See something
                values[2 + 4 * i] = sights[i].getDistance() / sightRange;
                values[3 + 4 * i] = sights[i].getAngle();
                if (sights[i].getElement() instanceof Vegetable) {
                    values[0 + 4 * i] = 1f;
                    values[1 + 4 * i] = 0;
                    values[4 + 4 * i] = sights[i].getElement().getSize() / default_radius;
                } else {
                    values[0 + 4 * i] = 0f;
                    values[1 + 4 * i] = 1f;
                    values[4 + 4 * i] = 100 - ((Creature) sights[i].getElement()).getHp() / 100;
                }
            }
        }
        values[8] = eating || killing ? 1 : 0;
        values[9] = prevHp > hp ? 1 : 0;
        // Compute brain
        float[] actions = null;
        try {
            actions = brain.compute(values);
        } catch (Exception ex) {
            // Should not happen
            Logger.getLogger(Creature.class.getName()).log(Level.SEVERE, null, ex);
        }
        Log.log(Log.DEBUG, "Accel: " + actions[0] + " RotClock: " + actions[1] + " RotAntiClock: " + actions[2] + " Beak: " + actions[3]);
        speed = (actions[0] * 2 - actions[4] / 2) * max_speed;
        rotSpeed = (actions[1] - actions[2]) / 3;
        /*if (rotSpeed > max_rot_speed) {
         rotSpeed = max_rot_speed;
         }
         if (rotSpeed < -max_rot_speed) {
         rotSpeed = -max_rot_speed;
         }*/
        beak = actions[3];
        if (beak > default_radius * 3) {
            beak = default_radius * 3;
        } else if (beak < 0) {
            beak = 0;
        }
        return true;
    }

    public void applyToWorld() {
        if (speed > max_speed) {
            speed = max_speed;
        }
        if (speed < -max_speed) {
            speed = -max_speed;
        }
        // apply speed and rot speed
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
        if (dir > 2 * Math.PI) {
            dir -= 2 * Math.PI;
        }
        if (dir < 0) {
            dir += 2 * Math.PI;
        }
    }

    @Override
    public void render(ShapeRenderer s) {
        if (prevHp > hp || killing) {
            // Kill/BeKilled mark
            s.set(ShapeRenderer.ShapeType.Filled);
            s.setColor(prevHp > hp ? 1 : 0, killing ? 1 : 0, 0, 1);
            s.circle(getX(), getY(), default_radius);
            s.set(ShapeRenderer.ShapeType.Line);
        }
        // Body
        s.setColor(1 - (hp / 100), hp / 100, 0, 1);
        s.circle(getX(), getY(), getSize());
        // Eye
        double relX = Math.cos(dir), relY = Math.sin(dir);
        float c0 = sights[0] != null ? sights[0].getDistance() / sightRange * 2 + sightRange : 1;
        float c1 = sights[1] != null ? sights[1].getDistance() / sightRange * 2 + sightRange : 1;
        s.setColor(c0, c1, sights[0] == null && sights[1] == null ? 1 : 0, 1);
        s.circle((float) relX * getSize() * 0.6f + getX(), (float) relY * getSize() * 0.6f + getY(), 3);
        // Beak
        s.setColor(beak / default_radius * 3, 1 - beak / default_radius * 3, 0, 1);
        s.line((float) (relX * getSize() * 0.8f + getX()), (float) (relY * getSize() * 0.8f + getY()), (float) (relX * getSize() * (1.5f + beak / default_radius * 3) + getX()), (float) (relY * getSize() * (1.5f + beak / default_radius * 3) + getY()));
        //FOV
        float degrees = fov * 180f / (float) Math.PI;
        float orient = dir * 180f / (float) Math.PI - degrees / 2;
        s.setColor(0.3f, 0.3f, 0.3f, 1);
        s.arc((float) relX * getSize() + getX(), (float) relY * getSize() + getY(), sightRange, orient, degrees);
    }

    public Sight[] interactWithWorld() {
        eating = false;
        Sight[] sights = new Sight[2];
        // Vegetables
        Element seen = null;
        float dist = 0, angle = 0, ndir = dir - (float) Math.PI;
        for (Vegetable e : Game.get().getWorld().getPlants()) {
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
            // Check if can eat
            if (overlaps(e)) {
                eating = true;
                if (hp < 100) {
                    e.setSize(e.getSize() - 0.5f);
                }
                if (e.getSize() <= 0) {
                    e.setSize(0);
                }
                hp++;
                fitness++;
                if (hp > 100) {
                    hp = 100;
                }
            }
        }
        if (seen != null) {
            sights[0] = new Sight(seen, dist, angle);
        } else {
            sights[0] = null;
        }
        // Creatures
        seen = null;
        dist = 0;
        angle = 0;
        ndir = dir - (float) Math.PI;
        killing = false;
        for (Creature e : Game.get().getWorld().getCreatures()) {
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
                if (Math.abs(relAngle - ndir) < fov) {
                    // Visible
                    seen = e;
                    angle = relAngle - ndir;
                    dist = tempDist;
                }
                //Log.log(Log.DEBUG,"RelAngle "+relAngle+" Dir "+ndir);
            }
            // Check if attackable
            if (tempDist < beak && Math.abs(relAngle - ndir) < fov / 4) {
                // Attacking!
                hp += beak / default_radius * 3;
                fitness += 10;
                if (hp > 100) {
                    hp = 100;
                }
                killing = true;
                e.setHp(e.getHp() - beak / default_radius * 3);
            }

        }
        if (seen != null) {
            sights[1] = new Sight(seen, dist, angle);
        } else {
            sights[1] = null;
        }
        return sights;
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
        hp = 100;
    }

    public boolean isDone() {
        return done;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = hp;
    }

    public Thread getThread() {
        return thread;
    }
}
