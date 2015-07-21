package logic;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Represents a dynamic simulation element with a circular shape
 *
 * @author fazo
 */
public abstract class Element {

    private float x, y, size;

    /**
     * Create an element at given position with given radius. Elements have a
     * circular shape.
     *
     * @param x the x position
     * @param y the y position
     * @param size the element body radius
     */
    public Element(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    /**
     * Calculate the distance between the circumference of this element and
     * another one's, taking into account the size. This means that two elements
     * whose circumference is touching will have a distance of 0, and two
     * overlapping elements will have a negative distance
     *
     * @param e the element whose distance from this one will be checked
     * @return the distance from the element. It's 0 if they are just touching,
     * negative if they are colliding and positive if they are not
     */
    public float distanceFrom(Element e) {
        return (float) Math.sqrt(Math.pow(e.x - x, 2) + Math.pow(e.y - y, 2)) - getSize() - e.getSize();
    }

    /**
     * Checks if this element overlaps another one, causing a collision
     *
     * @param e the element which position will be checked to see if it overlaps
     * this one
     * @return true if the elements are overlapping, causing a collision
     */
    public boolean overlaps(Element e) {
        return distanceFrom(e) < 0;
    }

    /**
     * Checks if this element overlaps a circular object, causing a collision
     *
     * @param x the position of the center of the circular object
     * @param y the position of the center of the circular object
     * @param radius the radius of the circular object
     * @return true if the object overlaps this one
     */
    public boolean overlaps(float x, float y, float radius) {
        return (float) Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2)) < getSize() + radius;
    }

    /**
     * Check if a point in space occupies the same space as this object
     *
     * @param x the x position of the point to check
     * @param y the y position of the point to check
     * @return true if the point is part of this object's area
     */
    public boolean overlaps(float x, float y) {
        return overlaps(x, y, 1);
    }

    /**
     * Translate this object is space
     *
     * @param deltaX x translation
     * @param deltaY y translation
     */
    public void move(float deltaX, float deltaY) {
        x += deltaX;
        y += deltaY;
    }

    /**
     * Run one iteration of this object's logic
     */
    public abstract void update();

    /**
     * Draw this object
     *
     * @param s the ShapeRenderer used to draw this object
     */
    public abstract void render(ShapeRenderer s);

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setSize(float size) {
        this.size = size;
    }

}
