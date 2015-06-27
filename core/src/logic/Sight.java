/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

/**
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
