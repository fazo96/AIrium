package logic.creatures;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Log;

/**
 *
 * @author fazo
 */
public abstract class BodyPart {

    protected int inputNeuronsUsed, outputNeuronsUsed;
    protected float angle, distFromCenter;
    protected float outputs[];
    protected Creature creature;

    public BodyPart(int inputNeuronsUsed, int outputNeuronsUsed, float angle, float distFromCenter, Creature creature) {
        this.inputNeuronsUsed = inputNeuronsUsed;
        this.angle = angle;
        this.distFromCenter = distFromCenter;
        this.creature = creature;
        outputs = new float[outputNeuronsUsed];
    }

    public abstract float[] update();

    protected abstract void draw(ShapeRenderer s, float relX, float relY);

    public void render(ShapeRenderer s) {
        double relX = Math.cos(creature.getDirection() + angle) * creature.getTorso().getRadius() * distFromCenter;
        double relY = Math.sin(creature.getDirection() + angle) * creature.getTorso().getRadius() * distFromCenter;
        draw(s, (float) relX, (float) relY);
    }
    
    protected abstract void useOutputs(float outputs[]);

    public int getInputNeuronsUsed() {
        return inputNeuronsUsed;
    }

    public int getOutputNeuronsUsed() {
        return outputNeuronsUsed;
    }

    public float getAngle() {
        return angle;
    }

    public float getDistanceFromCreatureCenter() {
        return distFromCenter;
    }

    public Creature getCreature() {
        return creature;
    }

}
