/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

/**
 * Represents a Action performed by an agent
 * @author fazo
 */
public class Action {
    private final Creature agent;
    private final Element victim;
    private final float damage;

    public Action(Creature agent, Element victim, float damage) {
        this.agent = agent;
        this.victim = victim;
        this.damage = damage;
    }

    public void apply(){
        if(victim instanceof Creature){
            Creature c = (Creature) victim;
            c.setHp(c.getHp()-damage);
        } else {
            victim.setSize(victim.getSize()-damage);
        }
    }
    
    public Creature getAgent() {
        return agent;
    }

    public Element getVictim() {
        return victim;
    }

    public float getDamage() {
        return damage;
    }
    
}
