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
    private Camera camera;
    private World world;

    @Override
    public void create() {
        game = this;
        world = new World(1920, 1080);
        shaper = new ShapeRenderer();
        camera = new Camera();
        //shaper.setAutoShapeType(true);
    }

    @Override
    public void render() {
        // Controls
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            world.newGen(false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(-camera.getSpeed(), 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(1+camera.getSpeed(), 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, -camera.getSpeed());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, camera.getSpeed());
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
        shaper.rect(camera.getX(), camera.getY(), world.getWidth(), world.getHeight());
        shaper.end();
    }

    public World getWorld() {
        return world;
    }

    public Camera getCamera() {
        return camera;
    }

    public static Game get() {
        return game;
    }
}
