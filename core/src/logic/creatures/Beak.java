/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.creatures;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import logic.Element;

/**
 *
 * @author fazo
 */
public class Beak extends BodyPart {

    private float length;
    private boolean attacking = false;
    public static float max_length = Torso.default_radius / 4, min_length = max_length / 4;

    public Beak(float angle, Creature creature) {
        super(3, 1, angle, 1, creature);
        length = min_length;
    }

    @Override
    public float[] act() {
        float r[] = new float[]{
            length / max_length, // current beak length
            length > max_length / 2 ? 1f : 0f, // wether the beak is doing damage
            attacking ? 1f : 0f
        };
        attacking = false;
        return r;
    }

    @Override
    public void interactWithElement(Element e, float distance, float relAngle) {
        if (e instanceof Creature && distance < length && length > max_length / 2 && Math.abs(relAngle) < 0.3f) {
            // Can attack
            creature.praise(Creature.pointsForAttacking);
            creature.getTorso().heal(length*Creature.hpForAttacking/2);
            ((Creature) e).getTorso().heal(-length*Creature.hpForAttacking/2);
            attacking = true;
        }
    }

    @Override
    protected void draw(ShapeRenderer s, float relX, float relY) {
        s.set(ShapeRenderer.ShapeType.Line);
        // Draw Beak
        s.setColor(getLength() / Beak.max_length, 1 - getLength() / Beak.max_length, 0, 1);
        s.line((float) (relX + creature.getX()), (float) (relY + creature.getY()), (float) (relX * (1.5f + getLength() / Beak.max_length) + creature.getX()), (float) (relY * (1.5f + getLength() / Beak.max_length) + creature.getY()));
        if (attacking) {
            s.circle((float) (relX * (1.5f + getLength() / Beak.max_length) + creature.getX()), (float) (relY * (1.5f + getLength() / Beak.max_length) + creature.getY()), 0.3f);
        }
    }

    @Override
    public void readFromBrain(float[] outputs) {
        length = outputs[0] * max_length;
        if (length > max_length) {
            length = max_length;
        } else if (length < 0) {
            length = 0;
        }
    }

    public float getLength() {
        return length;
    }

}
