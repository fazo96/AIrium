/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.game;

/**
 *
 * @author fazo
 */
public class Camera {

    private int x, y, speed;
    private float scale = 1;

    public Camera() {
        x = 0;
        y = 0;
        speed = 5;
    }

    public void translate(int deltaX, int deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public void zoomOut() {
        scale -= 0.001f;
    }

    public void zoomIn() {
        scale += 0.001f;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

}
