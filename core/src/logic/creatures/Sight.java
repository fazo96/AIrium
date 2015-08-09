package logic.creatures;

import logic.Element;

/**
 * Stores a sight of an element, as seen from a creature's eye
 *
 * @author fazo
 */
public class Sight {

    private Element seen;
    private float distance, angle;

    public Sight(Element seen, float distance, float angle) {
        this.seen = seen;
        this.distance = distance;
        this.angle = angle;
    }

    public Element getElement() {
        return seen;
    }

    public float getDistance() {
        return distance;
    }

    public float getAngle() {
        return angle;
    }

}
