package logic.creatures;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import logic.Element;
import logic.Vegetable;

/**
 *
 * @author fazo
 */
public class Torso extends BodyPart {

    private float hp, prevHp, radius, pain = 0;
    public static float default_radius = 20, max_hp = 100, hpDecay = 0.5f, eatingSpeed = 0.1f;
    private boolean eating = false;

    public Torso(Creature c) {
        super(3, 0, 0, 0, c);
        radius = default_radius;
        hp = max_hp;
        prevHp = hp;
    }

    @Override
    public void draw(ShapeRenderer s, float x, float y) {
        s.setColor(1 - (hp / max_hp), hp / max_hp, 0, 1);
        s.circle(x + creature.getX(), y + creature.getY(), radius);
        // Draw damage/heal marks
        s.set(ShapeRenderer.ShapeType.Filled);
        if (getReceivedDamage() > 0) {
            // Damage mark
            s.setColor(1, 0, 0, 1);
        } else if (getReceivedDamage() < 0 || eating) {
            // Heal mark
            s.setColor(0, 1, 0, 1);
        }
        if (getReceivedDamage() != 0 || eating) {
            s.circle(x + creature.getX(), y + creature.getY(), 5);
        }
    }

    @Override
    public float[] act() {
        // apply hunger
        hp -= hpDecay;
        float r[] = new float[]{
            hp/max_hp,
            eating ? 1f : 0f,
            pain
        };
        pain = 0;
        prevHp = hp;
        eating = false;
        return r;
    }

    @Override
    public void interactWithElement(Element e, float distance, float relAngle) {
        if (e instanceof Vegetable && distance < 0 && hp < max_hp) {
            e.setSize(e.getSize() - eatingSpeed);
                if (e.getSize() == 0) {
                    e.setSize(0);
                }
            heal(Creature.hpForEatingPlants);
            creature.praise(Creature.pointsForEatingPlants);
            eating = true;
        }
    }

    @Override
    public void readFromBrain(float[] data) {
        if (getReceivedDamage() > 0) {
            pain = -1;
        } else if (getReceivedDamage() < 0) {
            pain = 1;
        } else {
            pain = 0;
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    /**
     * Apply a modification to this creature's health. Can be negative.
     *
     * @param amount how much to heal/damage
     */
    public void heal(float amount) {
        hp += amount;
        if (hp < 0) {
            hp = 0;
        }
        if (hp > max_hp) {
            hp = max_hp;
        }
    }

    public float getReceivedDamage() {
        return prevHp - hp;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = hp;
    }

}
