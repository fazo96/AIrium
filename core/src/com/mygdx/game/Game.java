package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import logic.Element;
import logic.World;

public class Game extends ApplicationAdapter {

    private static Game game;
    SpriteBatch batch;
    ShapeRenderer shaper;
    Texture img;
    private World world;

    @Override
    public void create() {
        game = this;
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        world = new World(640, 480);
        shaper = new ShapeRenderer();
        //shaper.setAutoShapeType(true);
    }

    @Override
    public void render() {
        // Input
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            world.newGen();
        }
        // Update
        world.update();
        // Draw
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        /*batch.begin();
        batch.draw(img, 0, 0);
        batch.end();*/
        shaper.setColor(1, 1, 1, 1);
        shaper.begin(ShapeRenderer.ShapeType.Line);
        //shaper.circle(640, 480, 100);
        for(Element e: world.getElements()) e.render(shaper);
        shaper.end();
    }

    public World getWorld() {
        return world;
    }

    public static Game get() {
        return game;
    }
}
