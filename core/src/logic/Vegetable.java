/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game;

/**
 *
 * @author fazo
 */
public class Vegetable extends Element {

    public static final int default_radius = 5;
    private float x, y;

    public Vegetable(float x, float y) {
        super(x, y, default_radius);
    }

    @Override
    public void update() {
        if (getSize() <= 0) {
            Game.get().getWorld().getDeadPlants().add(this);
        }
    }

    @Override
    public void render(ShapeRenderer s) {
        s.setColor(1, 1, 1, 1);
        s.circle(getX(), getY(), getSize());
    }
}
