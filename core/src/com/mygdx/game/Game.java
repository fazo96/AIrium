package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ConcurrentModificationException;
import logic.Element;
import logic.World;

public class Game extends ApplicationAdapter {

    private static Game game;
    ShapeRenderer shaper;
    private World world;
    private float cameraSpeed = 15;
    private BitmapFont font;
    private boolean paused = false;

    @Override
    public void create() {
        game = this;
        world = new World(2500, 2500);
        shaper = new ShapeRenderer();
        shaper.setAutoShapeType(true);
        font = new BitmapFont();
        Thread worldThread = new Thread(world);
        worldThread.setName("Worker");
        worldThread.setPriority(Thread.MAX_PRIORITY);
        worldThread.start();
    }

    @Override
    public void render() {
        // Controls
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            world.newGen(false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            shaper.translate(-cameraSpeed, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            shaper.translate(cameraSpeed, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            shaper.translate(0, -cameraSpeed, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            shaper.translate(0, cameraSpeed, 0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PLUS)) {
            shaper.scale(0.5f, 0.5f, 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            shaper.scale(1.5f, 1.5f, 1);
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
        // Draw
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shaper.setColor(1, 1, 1, 1);
        shaper.begin(ShapeRenderer.ShapeType.Line);
        try {
            for (Element e : world.getElements()) {
                try {
                    e.render(shaper);
                } catch (ArrayIndexOutOfBoundsException ex) {
                // No idea why it happens, but it's rendering so meh
                    //Log.log(Log.ERROR, ex+"");
                }
            }
        } catch (ConcurrentModificationException ex) {
        }
        shaper.setColor(0.3f, 0.3f, 0.3f, 1);
        // draw borders
        shaper.rect(0, 0, world.getWidth(), world.getHeight());
        shaper.end();
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
