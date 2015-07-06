package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ConcurrentModificationException;
import logic.Creature;
import logic.Element;
import logic.World;

public class Game extends ApplicationAdapter {

    private static Game game;
    ShapeRenderer renderer, overlayRenderer;
    private World world;
    private float cameraSpeed = 15;
    private BitmapFont font;
    private boolean paused = false;

    @Override
    public void create() {
        game = this;
        renderer = new ShapeRenderer();
        renderer.setAutoShapeType(true);
        overlayRenderer = new ShapeRenderer();
        overlayRenderer.setAutoShapeType(true);
        font = new BitmapFont();
        Thread worldThread = new Thread(world);
        worldThread.setName("Worker");
        worldThread.setPriority(Thread.MAX_PRIORITY);
        worldThread.start();
    }

    public Game() {
        world = new World(2500, 2500);
    }

    @Override
    public void render() {
        // Controls
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            world.launchNewGen();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            renderer.translate(-cameraSpeed, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            renderer.translate(cameraSpeed, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            renderer.translate(0, -cameraSpeed, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            renderer.translate(0, cameraSpeed, 0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PLUS)) {
            renderer.scale(0.5f, 0.5f, 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            renderer.scale(1.5f, 1.5f, 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            if (world.getFpsLimit() == 60) {
                world.setFpsLimit(0);
            } else {
                world.setFpsLimit(60);
            }
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            renderer.translate(Gdx.input.getDeltaX(), Gdx.input.getDeltaY() * -1, 0);
        }
        /*
         // Broken for now
         if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
         // TODO: project coordinates to world
         world.selectCreatureAt(Gdx.input.getX(), Gdx.input.getY());
         }*/
        // Draw
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        try {
            for (Element e : world.getElements()) {
                try {
                    e.render(renderer);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // No idea why it happens, but it's rendering so meh
                    //Log.log(Log.ERROR, ex+"");
                }
            }
        } catch (ConcurrentModificationException ex) {
        }
        renderer.setColor(0.3f, 0.3f, 0.3f, 1);
        // draw borders
        renderer.rect(0, 0, world.getWidth(), world.getHeight());
        if (world.getSelectedCreature() != null) {
            // There is a selection
            Creature c = world.getSelectedCreature();
            renderer.setColor(1, 1, 1, 1);
            // Draw selection rectangle
            renderer.rect(c.getX() - c.getSize() / 2, c.getY() - c.getSize() / 2, c.getSize(), c.getSize());
            // Draw brain
            overlayRenderer.begin();
            c.getBrain().render(overlayRenderer);
            overlayRenderer.end();
        }
        renderer.end();
    }

    public World getWorld() {
        return world;
    }

    public static Game get() {
        return game;
    }

    public boolean isPaused() {
        return paused;
    }
}
