package logic;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Game;

/**
 * Represents a small plant-like vegetable with circular shape
 * @author fazo
 */
public class Vegetable extends Element {

    public static double default_radius = 5;
    private double decayRate = 0;

    public Vegetable(double x, double y) {
        super(x, y, default_radius);
    }

    @Override
    public void update() {
        setSize(getSize()-decayRate);
        if (getSize() <= 2) {
            Game.get().getWorld().getDeadPlants().add(this);
        }
    }

    @Override
    public void render(ShapeRenderer s) {
        s.setColor(1, 1, 1, 1);
        s.circle((float) getX(), (float) getY(), (float) getSize());
    }

    public double getDecayRate() {
        return decayRate;
    }

    public void setDecayRate(double decayRate) {
        this.decayRate = decayRate;
    }
}
