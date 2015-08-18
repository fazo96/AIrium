/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.game;

/**
 *
 * @author Fazo
 */
public abstract class Listener {

    public static int FPS_CHANGED = 0, CREATURE_LIST_CHANGED = 1, PAUSED_OR_RESUMED = 2;

    public void pollAndHandleEvents() {
        if(Game.get() == null || Game.get().getWorld() == null) return;
        while(Game.get().getWorld().getEventQueue().size() > 0) {
            on(Game.get().getWorld().getEventQueue().poll());
        }
    }

    public abstract void on(int event);
}
