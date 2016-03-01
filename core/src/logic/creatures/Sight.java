package logic.creatures;

import logic.Element;

/**
 * Stores a sight of an element, as seen from a creature's eye
 *
 * @author fazo
 */
public class Sight {

    private Element seen;
    private double distance, angle;

    public Sight(Element seen, double distance, double angle) {
        this.seen = seen;
        this.distance = distance;
        this.angle = angle;
    }

    public Element getElement() {
        return seen;
    }

    public double getDistance() {
        return distance;
    }

    public double getAngle() {
        return angle;
    }

}
