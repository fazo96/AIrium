package logic.creatures;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import logic.Element;

/**
 *
 * @author fazo
 */
public abstract class BodyPart {

    protected int inputNeuronsUsed;
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

    /**
     * Prepare data to be sent to the brain
     *
     * @return the data to send to the brain, must be inputNeuronsUsed long
     */
    public abstract float[] act();
    
    /**
     * Interact with another element
     *
     * @param e the Element (creature or plant)
     * @param distance the distance
     * @param relAngle the relative angle
     */
    public abstract void interactWithElement(Element e, float distance, float relAngle);

    /**
     * Receive some data from the brain
     *
     * @param data the data received from the brain, will be outputNeuronsUsed
     * long
     */
    public abstract void readFromBrain(float data[]);

    protected abstract void draw(ShapeRenderer s, float relX, float relY);

    public void render(ShapeRenderer s) {
        double relX = Math.cos(creature.getDirection() + angle) * creature.getTorso().getRadius() * distFromCenter;
        double relY = Math.sin(creature.getDirection() + angle) * creature.getTorso().getRadius() * distFromCenter;
        draw(s, (float) relX, (float) relY);
    }

    public int getInputNeuronsUsed() {
        return inputNeuronsUsed;
    }

    public int getOutputNeuronsUsed() {
        return outputs.length;
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
