package logic.creatures;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 *
 * @author fazo
 */
public class Torso extends BodyPart {

    private float hp, prevHp, radius;
    public static float default_radius = 20, max_hp = 100, hpDecay = 0.5f;

    public Torso(Creature c) {
        super(2, 0, 0, 0, c);
        radius = default_radius;
        hp = max_hp;
        prevHp = hp;
    }

    @Override
    public void draw(ShapeRenderer s, float x, float y) {
        s.setColor(1 - (hp / max_hp), hp / max_hp, 0, 1);
        s.circle(x+creature.getX(), y+creature.getY(), radius);
        // Draw damage/heal marks
        s.set(ShapeRenderer.ShapeType.Filled);
        if (getReceivedDamage() > 0) {
            // Damage mark
            s.setColor(1, 0, 0, 1);
        } else if (getReceivedDamage() < 0) {
            // Heal mark
            s.setColor(0, 1, 0, 1);
        }
        if (getReceivedDamage() != 0) {
            s.circle(x, y, 5);
        }
    }

    @Override
    protected void useOutputs(float[] outputs) {
    }

    @Override
    public float[] update() {
        // apply hunger
        hp -= hpDecay;
        prevHp = hp;
        return new float[]{};
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
