package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import java.util.ConcurrentModificationException;
import java.util.Map;
import logic.Creature;
import logic.Element;
import logic.World;

public class Game extends ApplicationAdapter {

    private static Game game;
    ShapeRenderer renderer, overlayRenderer;
    private World world;
    private OrthographicCamera camera;
    private boolean paused = false;
    private InputProcessor input;

    @Override
    public void create() {
        game = this;
        input = new InputProcessor() {

            @Override
            public boolean keyDown(int i) {
                return true;
            }

            @Override
            public boolean keyUp(int i) {
                return true;
            }

            @Override
            public boolean keyTyped(char c) {
                return true;
            }

            @Override
            public boolean touchDown(int x, int y, int button, int pointer) {
                Vector3 v = camera.unproject(new Vector3(x, y, 0));
                world.selectCreatureAt(Math.round(v.x), Math.round(v.y));
                return true;
            }

            @Override
            public boolean touchUp(int i, int i1, int i2, int i3) {
                return true;
            }

            @Override
            public boolean touchDragged(int i, int i1, int i2) {
                //renderer.translate(Gdx.input.getDeltaX(), -Gdx.input.getDeltaY(), 0);
                camera.translate(-Gdx.input.getDeltaX()*camera.zoom, Gdx.input.getDeltaY()*camera.zoom);
                camera.update();
                return true;
            }

            @Override
            public boolean mouseMoved(int i, int i1) {
                return true;
            }

            @Override
            public boolean scrolled(int i) {
                /*
                if (i>0) {
                    renderer.scale(0.9f, 0.9f, 1);
                } else {
                    renderer.scale(1.1f, 1.1f, 1);
                }
                */
                camera.zoom += i;
                if(camera.zoom < 1f) camera.zoom = 1f;
                else if(camera.zoom > 10) camera.zoom = 10;
                camera.update();
                Log.log(Log.DEBUG, "Camera zoom: "+camera.zoom+" Delta: "+i);
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
        renderer = new ShapeRenderer();
        renderer.setAutoShapeType(true);
        overlayRenderer = new ShapeRenderer();
        overlayRenderer.setAutoShapeType(true);
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        camera.update();
        Thread worldThread = new Thread(world);
        worldThread.setName("Worker");
        worldThread.setPriority(Thread.MAX_PRIORITY);
        worldThread.start();
    }

    public Game() {
        this(null);
    }

    public Game(Map<String, Float> options) {
        world = new World(options);
    }

    @Override
    public void resize(int width, int height){
        camera.setToOrtho(false, width, height);
        camera.update();
    }
    
    @Override
    public void render() {
        // Draw
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        try {
            for (Element e : world.getElements()) {
                if (e == null) {
                    // Yeah, the perks of multithreading I guess
                    continue;
                }
                try {
                    e.render(renderer);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // Render only half the elements because the list gets
                    // modified by another thread? Who cares, it's a simulation
                    // not some videogame
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
            renderer.rect(c.getX() - c.getSize(), c.getY() - c.getSize(), c.getSize() * 2, c.getSize() * 2);
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

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (world != null) {
            world.fire(Listener.PAUSED_OR_RESUMED);
        }
    }
}
