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
    protected double angle, distFromCenter;
    protected double outputs[];
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
    public BodyPart(int inputNeuronsUsed, int outputNeuronsUsed, double angle, double distFromCenter, Creature creature) {
        this.inputNeuronsUsed = inputNeuronsUsed;
        this.angle = angle;
        this.distFromCenter = distFromCenter;
        this.creature = creature;
        outputs = new double[outputNeuronsUsed];
    }

    /**
     * Prepare data to be sent to the brain. This is called once every frame,
     * before the interactions with other elements.
     *
     * @return the data to send to the brain, must be inputNeuronsUsed long
     */
    public abstract double[] act();

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
    public abstract void interactWithElement(Element e, double distance, double relAngle);

    /**
     * Receive some data from the brain. This is called once every frame, after
     * interactions with other elements.
     *
     * @param data the data received from the brain, will be outputNeuronsUsed
     * long
     */
    public abstract void readFromBrain(double data[]);

    /**
     * This will be called when the
     *
     * @param s the ShapeRenderer used to draw this body part.
     * @param relX the X position of this bodypart relative to the center its
     * creature
     * @param relY the Y position of this bodypart relative to the center its
     * creature
     */
    protected abstract void draw(ShapeRenderer s, double relX, double relY);

    /**
     * Prepares data and calls draw
     *
     * @param s the ShapeRenderer used to draw this body part.
     */
    public final void render(ShapeRenderer s) {
        double relX = Math.cos(creature.getDirection() + angle) * creature.getTorso().getRadius() * distFromCenter;
        double relY = Math.sin(creature.getDirection() + angle) * creature.getTorso().getRadius() * distFromCenter;
        draw(s, (double) relX, (double) relY);
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
    public double getAngle() {
        return angle;
    }

    public double getDistanceFromCreatureCenter() {
        return distFromCenter;
    }

    public Creature getCreature() {
        return creature;
    }

}
