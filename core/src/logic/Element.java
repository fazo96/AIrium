/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 *
 * @author fazo
 */
public abstract class Element {

    private float x, y, size;

    public Element(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public float distanceFrom(Element e) {
        return (float) Math.sqrt(Math.pow(e.x - x, 2) + Math.pow(e.y - y, 2)) - getSize() - e.getSize();
    }

    public boolean overlaps(Element e) {
        return distanceFrom(e) < 0;
    }

    public boolean overlaps(float x, float y, float radius) {
        return (float) Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2)) < getSize() + radius;
    }

    public boolean overlaps(float x, float y) {
        return overlaps(x, y, 1);
    }

    public void move(float deltaX, float deltaY) {
        x += deltaX;
        y += deltaY;
    }

    /**
     * Update element logic
     *
     * @return true if element is still alive
     */
    public abstract boolean update();

    /**
     * Draw the element
     *
     * @param s the instance used to draw shapes
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
