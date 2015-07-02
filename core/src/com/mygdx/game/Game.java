package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import logic.Element;
import logic.World;

public class Game extends ApplicationAdapter {

    private static Game game;
    ShapeRenderer shaper;
    private World world;
    private float cameraSpeed = 5;

    @Override
    public void create() {
        game = this;
        world = new World(1920, 1080);
        shaper = new ShapeRenderer();
        //shaper.setAutoShapeType(true);
    }

    @Override
    public void render() {
        // Controls
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            world.newGen(false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            shaper.translate(-cameraSpeed, 0,0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            shaper.translate(cameraSpeed, 0,0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            shaper.translate(0, -cameraSpeed,0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            shaper.translate(0, cameraSpeed,0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PLUS)) {
            shaper.scale(0.3f, 0.3f, 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            shaper.scale(1f, 1f, 1);
        }
        // Update
        world.update();
        // Draw
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shaper.setColor(1, 1, 1, 1);
        shaper.begin(ShapeRenderer.ShapeType.Line);
        for (Element e : world.getElements()) {
            e.render(shaper);
        }
        shaper.setColor(0.3f, 0.3f, 0.3f, 1);
        shaper.rect(0, 0, world.getWidth(), world.getHeight());
        shaper.end();
    }

    public World getWorld() {
        return world;
    }

    public static Game get() {
        return game;
    }
}
