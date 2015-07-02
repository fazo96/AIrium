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

    public static final int default_radius = 20;
    public static final float max_speed = 3;

    private Brain brain;
    private float dir, hp, speed, sightRange, fov, fitness, rotSpeed;
    private boolean eating = false;
    private Sight sight;

    public Creature(float x, float y) {
        super(x, y, default_radius);
        dir = (float) (Math.random() * 2 * Math.PI);
        hp = 100;
        speed = 0;//(float) Math.random() * 3;
        rotSpeed = 0;//(float) Math.random() - 0.5f;
        sightRange = 60;
        fov = (float) Math.PI / 1.5f;
        fitness = 0;
        brain = new Brain(4, 3, 1, 6);
    }

    @Override
    public void update() {
        // apply hunger
        hp -= 0.5f;
        if (hp < 0) {
            Game.get().getWorld().getGraveyard().add(this);
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
            setX(Game.get().getWorld().getWidth()+getX());
        }
        if (getY() < 0) {
            setX(Game.get().getWorld().getHeight()+getY());
        }
        if (getX() > Game.get().getWorld().getWidth()) {
            setX(getX()-Game.get().getWorld().getWidth());
        }
        if (getY() > Game.get().getWorld().getHeight()) {
            setY(getY()-Game.get().getWorld().getHeight());
        }
        dir += rotSpeed;
        // try eating
        eat();
        //fitness -= 0.1;
        if (dir > 2 * Math.PI) {
            dir -= 2 * Math.PI;
        }
        if (dir < 0) {
            dir += 2 * Math.PI;
        }
        sight = look(); // take a look
        // feed data to brain
        float[] values = new float[4];
        // 0: see food
        // 1: distance
        // 2: angle
        // 3: food sensor
        if (sight == null || sight.getElement() == null) {
            values[0] = 0;
            values[1] = 0;
            values[2] = 0;
        } else if (sight.getElement() instanceof Vegetable) {
            values[1] = 1;
            values[1] = sight.getDistance() / sightRange;
            values[2] = sight.getAngle();
        } else {
            values[0] = 0f;
            values[1] = sight.getDistance() / sightRange;
            values[2] = sight.getAngle();
        }
        values[3] = eating ? 1 : 0;
        try {
            brain.input(values);
        } catch (Exception ex) {
            // Should not happen
            Logger.getLogger(Creature.class.getName()).log(Level.SEVERE, null, ex);
        }
        // compute behavior
        float[] actions = brain.compute();
        Log.log(Log.DEBUG, "Accel: " + actions[0] + " RotClock: " + actions[1] + " RotAntiClock: " + actions[2]);
        speed = actions[0] * max_speed;
        rotSpeed = actions[1] - actions[2];
    }

    public void setHp(float hp) {
        this.hp = hp;
    }

    @Override
    public void render(ShapeRenderer s) {
        // Body
        s.setColor(1 - (hp / 100), hp / 100, 0, 1);
        s.circle(getX() + Game.get().getCamera().getX(), getY() + Game.get().getCamera().getY(), getSize());
        // Eye
        double relX = Math.cos(dir) * getSize(), relY = Math.sin(dir) * getSize();
        s.setColor(1, 1, 1, 1);
        if (sight != null) {
            float c = sight.getDistance() / sightRange * 2 + sightRange;
            if (sight.getElement() instanceof Creature) {
                s.setColor(c, 0, 0, 1);
            } else if (sight.getElement() instanceof Vegetable) {
                s.setColor(0, c, 0, 1);
            }
        }
        s.circle((float) relX + getX() + Game.get().getCamera().getX(), (float) relY + getY() + Game.get().getCamera().getY(), 3);
        //FOV
        float degrees = fov * 180f / (float) Math.PI;
        float orient = dir * 180f / (float) Math.PI - degrees / 2;
        s.setColor(0.3f, 0.3f, 0.3f, 1);
        s.arc((float) relX + getX() + Game.get().getCamera().getX(), (float) relY + getY() + Game.get().getCamera().getY(), sightRange, orient, degrees);
    }

    public Sight look() {
        Element seen = null;
        float dist = 0, angle = 0, ndir = dir - (float) Math.PI;
        for (Element e : Game.get().getWorld().getElements()) {
            if (e == this) {
                continue;
            }
            float tempDist = distanceFrom(e);
            if (tempDist > sightRange) {
                continue;
            }
            //Log.log(Log.DEBUG,"TempDist "+tempDist+" SightRange "+sightRange);
            if (tempDist > dist && seen != null) {
                continue;
            }
            float relAngle = (float) (Math.atan2(getY() - e.getY(), getX() - e.getX()));
            //if((relAngle > dir-fov/2 && relAngle < dir+fov/2)){
            if (Math.abs(relAngle - ndir) < fov) {
                // Visible
                seen = e;
                angle = relAngle - ndir;
                dist = tempDist;
            }
            //Log.log(Log.DEBUG,"RelAngle "+relAngle+" Dir "+ndir);
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
                if(e.getSize() == 0) e.setSize(0);
                hp++;
                fitness++;
                if (hp > 100) {
                    hp = 100;
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
        hp = 100;
    }

}
