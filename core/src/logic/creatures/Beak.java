/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.creatures;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import static logic.creatures.Creature.fov;
import static logic.creatures.Creature.hpForAttacking;
import static logic.creatures.Creature.pointsForAttacking;

/**
 *
 * @author fazo
 */
public class Beak extends BodyPart {

    private float length;
    public static float max_length = Torso.default_radius / 4, min_length = max_length / 4;

    public Beak(float angle, Creature creature) {
        super(1, 1, angle, 1, creature);
        length = min_length;
    }

    @Override
    public float[] update() {
        return new float[]{};
    }
    
    @Override
    protected void draw(ShapeRenderer s, float relX, float relY) {
        s.set(ShapeRenderer.ShapeType.Line);
        // Draw Beak
        s.setColor(getLength() / Beak.max_length, 1 - getLength() / Beak.max_length, 0, 1);
        s.line((float) (relX + creature.getX()), (float) (relY + creature.getY()), (float) (relX * (1.5f + getLength() / Beak.max_length) + creature.getX()), (float) (relY * (1.5f + getLength() / Beak.max_length) + creature.getY()));
    }

    @Override
    protected void useOutputs(float[] outputs) {
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
