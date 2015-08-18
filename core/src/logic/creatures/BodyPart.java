package logic.creatures;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import logic.Element;

/**
 * A body part. Used in creatures. Extend this class to create custom body
 * parts.
 *
 * @author fazo
 */
public abstract class BodyPart {

    protected int inputNeuronsUsed;
    protected float angle, distFromCenter;
    protected float outputs[];
    protected Creature creature;

    /**
     * Create an instance of a Body Part.
     *
     * @param inputNeuronsUsed how many input neurons it'll need
     * @param outputNeuronsUsed how many output neurons it'll need
     * @param angle the angle relative to the center of the creature
     * @param distFromCenter how distance from the center of the creature is
     * this body part
     * @param creature the creature that owns this body part
     */
    public BodyPart(int inputNeuronsUsed, int outputNeuronsUsed, float angle, float distFromCenter, Creature creature) {
        this.inputNeuronsUsed = inputNeuronsUsed;
        this.angle = angle;
        this.distFromCenter = distFromCenter;
        this.creature = creature;
        outputs = new float[outputNeuronsUsed];
    }

    /**
     * Prepare data to be sent to the brain. This is called once every frame,
     * before the interactions with other elements.
     *
     * @return the data to send to the brain, must be inputNeuronsUsed long
     */
    public abstract float[] act();

    /**
     * Interact with another element. This will be called every time the body
     * part has a chance to interact with another element. act() will be called
     * once every frame, before all the interactions. readFromBrain will be
     * called once every frame, after all the interactions.
     *
     * @param e the Element (creature or plant)
     * @param distance the distance
     * @param relAngle the relative angle
     */
    public abstract void interactWithElement(Element e, float distance, float relAngle);

    /**
     * Receive some data from the brain. This is called once every frame, after
     * interactions with other elements.
     *
     * @param data the data received from the brain, will be outputNeuronsUsed
     * long
     */
    public abstract void readFromBrain(float data[]);

    /**
     * This will be called when the
     *
     * @param s the ShapeRenderer used to draw this body part.
     * @param relX the X position of this bodypart relative to the center its
     * creature
     * @param relY the Y position of this bodypart relative to the center its
     * creature
     */
    protected abstract void draw(ShapeRenderer s, float relX, float relY);

    /**
     * Prepares data and calls draw
     *
     * @param s the ShapeRenderer used to draw this body part.
     */
    public final void render(ShapeRenderer s) {
        double relX = Math.cos(creature.getDirection() + angle) * creature.getTorso().getRadius() * distFromCenter;
        double relY = Math.sin(creature.getDirection() + angle) * creature.getTorso().getRadius() * distFromCenter;
        draw(s, (float) relX, (float) relY);
    }

    /**
     *
     * @return how many input neurons are used by this body part
     */
    public int getInputNeuronsUsed() {
        return inputNeuronsUsed;
    }

    /**
     *
     * @return how many output neurons are used by this body part
     */
    public int getOutputNeuronsUsed() {
        return outputs.length;
    }

    /**
     *
     * @return the angle of this bodypart relative to the center of the creature
     */
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
