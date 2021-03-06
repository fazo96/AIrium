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
import logic.Vegetable;

/**
 *
 * @author fazo
 */
public class Eye extends BodyPart {

    private Sight sights[];
    private int farthest = -1, seen;
    private double farthestDistance = 0;
    public static double fov = 2, sightRange = 30;

    public Eye(int nSights, double angle, Creature creature) {
        super(6 * nSights, 0, angle, 0.8f, creature);
        sights = new Sight[nSights];
        seen = 0;
    }

    @Override
    public double[] act() {
        double ret[] = new double[inputNeuronsUsed];
        int j = 0;
        for (int i = 0; i < sights.length; i++) {
            if (i < seen) {
                // Saw something
                ret[j] = 1;
                ret[j + 1] = sights[i].getElement() instanceof Creature ? 1 : 0;
                ret[j + 2] = sights[i].getDistance() / sightRange;
                ret[j + 3] = sights[i].getAngle();
                if (sights[i].getElement() instanceof Creature) {
                    ret[i + 4] = ((Creature) sights[i].getElement()).getTorso().getHp() / Torso.max_hp;
                    ret[i + 5] = ((Creature) sights[i].getElement()).getDangerLevel();
                } else {
                    ret[i + 4] = ((Vegetable) sights[i].getElement()).getSize() / Vegetable.default_radius;
                    ret[i + 5] = 0;
                }
            } else {
                // Saw nothing
                for (int z = 0; z < 6; z++) {
                    ret[j + z] = 0;
                }
            }
            j += 6;
        }
        seen = 0;
        farthest = -1;
        farthestDistance = 0;
        sights = new Sight[sights.length];
        return ret;
    }

    @Override
    public void interactWithElement(Element e, double distance, double angle) {
        if (e != creature && distance < sightRange && (distance < farthestDistance || seen < sights.length) && Math.abs(angle) < fov / 2) {
            if (seen < sights.length) {
                sights[seen] = new Sight(e, distance, angle);
                Log.log(Log.DEBUG,"Adding Sight number "+seen);
                seen++;
            } else {
                Log.log(Log.DEBUG,"Substituting Farthest");
                sights[farthest] = new Sight(e, distance, angle);
                farthest = -1;
            }
            for (int i = 0; i < seen; i++) {
                Sight s = sights[i];
                if (s.getDistance() > farthestDistance || farthest < 0) {
                    farthestDistance = s.getDistance();
                    farthest = i;
                }
            }
            Log.log(Log.DEBUG,"Seen " + seen + "/" + sights.length + ". Farthest is now " + farthest + " at " + farthestDistance);
        }
    }

    @Override
    protected void draw(ShapeRenderer s, double relX, double relY) {
        // Draw eye
        s.setColor(1, 1, 1, 1);
        s.circle((float) (creature.getX() + relX), (float) (creature.getY() + relY), 3);
        // Draw FOV cone
        double degrees = fov * 360f / (double) Math.PI;
        double orient = (creature.getDirection() + angle) * 180f / (double) Math.PI - degrees / 2;
        if (Game.get().getWorld().getOptions().getOrDefault("draw_view_cones", 0d) > 0) {
            s.setColor(0.3f, 0.3f, 0.3f, 1);
            s.arc((float) (relX + creature.getX()), (float) (relY + creature.getY()), (float) sightRange, (float) orient, (float) degrees);
        }
        // Sight Lines
        double c = 0;
        if (Game.get().getWorld().getOptions().getOrDefault("draw_sight_lines", 0d) > 0) {
            for (Sight sight : sights) {
                if (sight != null) {
                    c = sight.getDistance() / sightRange * 2 + sightRange;
                    if (sight.getElement() instanceof Creature) {
                        s.setColor((float) c, 0, 0, 1);
                    } else if (sight.getElement() instanceof Vegetable) {
                        s.setColor(0, (float) c, 0, 1);
                    }
                    s.line((float) (relX + creature.getX()), (float) (creature.getY() + relY), (float) (sight.getElement().getX()), (float) (sight.getElement().getY()));
                }
            }
        }
    }

    @Override
    public void readFromBrain(double[] data) {
    }

}
