/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.creatures;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game;
import com.mygdx.game.Log;
import logic.Element;

/**
 *
 * @author Fazo
 */
public class Movement extends BodyPart {

    private float speed = 0, rotSpeed = 0;
    public static float max_speed = 3;

    public Movement(Creature creature) {
        super(0, 4, 0, 0, creature);
    }

    @Override
    public float[] act() {
        if (speed > max_speed) {
            speed = max_speed;
        }
        if (speed < -max_speed) {
            speed = -max_speed;
        }
        // apply speed
        float xMul = (float) Math.cos(creature.getDirection()), yMul = (float) Math.sin(creature.getDirection());
        creature.move(xMul * speed, yMul * speed);
        if (creature.getX() < 0) {
            creature.setX(Game.get().getWorld().getWidth() + creature.getX());
        } else if (creature.getX() > Game.get().getWorld().getWidth()) {
            creature.setX(creature.getX() - Game.get().getWorld().getWidth());
        }
        if (creature.getY() < 0) {
            creature.setY(Game.get().getWorld().getHeight() + creature.getY());
        } else if (creature.getY() > Game.get().getWorld().getHeight()) {
            creature.setY(creature.getY() - Game.get().getWorld().getHeight());
        }
        creature.rotate(rotSpeed);
        return new float[]{};
    }

    @Override
    public void interactWithElement(Element e, float distance, float relAngle) {
    }

    @Override
    protected void draw(ShapeRenderer s, float relX, float relY) {
    }

    @Override
    public void readFromBrain(float[] data) {
        Log.log(Log.DEBUG, "Fowward: " + data[0] + "Back: " + data[1] + " Rot: " + data[2] + " RotAnti: " + data[3]);
        speed = (data[0] * 2 - data[1] / 2) * max_speed;
        rotSpeed = data[2] - data[3];
    }

}
