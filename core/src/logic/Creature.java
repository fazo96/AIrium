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
public class Creature extends Element {

    public static final int default_radius = 20, maxHp = 100;
    public static final float max_speed = 3, max_beak = default_radius / 4;

    private Brain brain;
    private float dir, hp, prevHp, speed, sightRange, fov, fitness, rotSpeed, beak;
    private boolean eating = false, killing = false;
    private Sight sight;

    public Creature(float x, float y) {
        super(x, y, default_radius);
        dir = (float) (Math.random() * 2 * Math.PI);
        hp = maxHp;
        prevHp = hp;
        speed = 0;//(float) Math.random() * 3;
        rotSpeed = 0;//(float) Math.random() - 0.5f;
        sightRange = 100;
        fov = (float) Math.PI / 2.5f;
        fitness = 0;
        brain = new Brain(6, 5, 2, 10);
    }

    @Override
    public void update() {
        // apply hunger
        hp -= 0.5f;
        prevHp = hp;
        if (hp < 0) { // Dead
            Game.get().getWorld().getGraveyard().add(this);
            Vegetable carcass = new Vegetable(getX(), getY());
            carcass.setSize(getSize());
            //carcass.setDecayRate(0.01f);
            Game.get().getWorld().add(carcass);
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
        sight = interactWithWorld();
        // feed data to brain
        float[] values = new float[brain.getNeurons()[0].length];
        // 0: sight: see food?
        // 1: sight: see creat?
        // 2: sight: distance
        // 3: sight: angle
        // 4: sight: size for vegetables, hunger for creatures
        // 5: food sensor
        if (sight == null || sight.getElement() == null) {
            // See nothing
            values[0] = 0;
            values[1] = 0;
            values[2] = 0;
            values[3] = 0;
            values[4] = 0;
        } else {
            // See something
            values[2] = sight.getDistance() / sightRange;
            values[3] = sight.getAngle();
            if (sight.getElement() instanceof Vegetable) {
                values[0] = 1f;
                values[1] = 0;
                values[4] = sight.getElement().getSize() / default_radius;
            } else {
                values[0] = 0f;
                values[1] = 1f;
                values[4] = maxHp - ((Creature) sight.getElement()).getHp() / maxHp;
            }
        }
        values[5] = eating ? 1 : 0;
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
        // Body
        s.setColor(1 - (hp / maxHp), hp / maxHp, 0, 1);
        s.circle(getX(), getY(), getSize());
        // Vision
        double relX = Math.cos(dir), relY = Math.sin(dir);
        float c = 0;
        if (sight != null) {
            c = sight.getDistance() / sightRange * 2 + sightRange;
        } else {
            s.setColor(1, 1, 1, 1);
        }
        float eyeX = (float) (relX * getSize() * 0.6f), eyeY = (float) (relY * getSize() * 0.6f);
        if (sight != null) {
            s.line(eyeX + getX(), getY() + eyeY, sight.getElement().getX(), sight.getElement().getY());
            if (sight.getElement() instanceof Creature) {
                s.setColor(c, 0, 0, 1);
            } else if (sight.getElement() instanceof Vegetable) {
                s.setColor(0, c, 0, 1);
            }
        }
        s.circle(getX() + eyeX, getY() + eyeY, 3);
        //FOV
        float degrees = fov * 180f / (float) Math.PI;
        float orient = dir * 180f / (float) Math.PI - degrees / 2;
        s.setColor(0.3f, 0.3f, 0.3f, 1);
        s.arc((float) eyeX + getX(), (float) eyeY + getY(), sightRange, orient, degrees);
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
        // Beak
        s.setColor(beak / max_beak, 1 - beak / max_beak, 0, 1);
        s.line((float) (relX * getSize() * 0.8f + getX()), (float) (relY * getSize() * 0.8f + getY()), (float) (relX * getSize() * (1.5f + beak / max_beak) + getX()), (float) (relY * getSize() * (1.5f + beak / max_beak) + getY()));
    }

    public Sight interactWithWorld() {
        Element seen = null;
        float dist = 0, angle = 0, ndir = dir - (float) Math.PI;
        killing = false;
        eating = false;
        for (Element e : Game.get().getWorld().getElements()) {
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
            // Check if eatable
            if (e instanceof Vegetable && tempDist < 0) {
                // Eat
                eating = true;
                e.setSize(e.getSize() - 0.1f);
                if (e.getSize() == 0) {
                    e.setSize(0);
                }
                hp++;
                fitness++;
                if (hp > maxHp) {
                    hp = maxHp;
                }
            }
            // Check if attackable
            if (e instanceof Creature && beak > 5 && tempDist < beak * 1.5f && Math.abs(relAngle - ndir) < (float) Math.PI / 10f) {
                // Attacking!
                hp++;
                fitness++;
                if (hp > maxHp) {
                    hp = maxHp;
                }
                killing = true;
                Creature c = (Creature) e;
                c.setHp(c.getHp() - 0.2f);
            }

        }
        if (seen != null) {
            return new Sight(seen, dist, angle);
        } else {
            return null;
        }
    }

    public void eat() {
        eating = false;
        for (Element e : Game.get().getWorld().getPlants()) {
            if (overlaps(e)) {
                eating = true;
                e.setSize(e.getSize() - 0.1f);
                if (e.getSize() == 0) {
                    e.setSize(0);
                }
                hp++;
                fitness++;
                if (hp > maxHp) {
                    hp = maxHp;
                }
            }
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
        hp = maxHp;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = hp;
    }
}
