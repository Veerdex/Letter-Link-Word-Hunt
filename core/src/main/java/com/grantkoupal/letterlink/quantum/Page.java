package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Purpose of Page is to provide organization for different scenes in a game
 */
public abstract class Page implements Disposable{
    protected Stage stage;

    protected Renderer renderer = new Renderer(new ShapeRenderer(), new SpriteBatch());

    public List<Timer> timers = new ArrayList<Timer>();
    public List<Animation> animations = new ArrayList<Animation>();
    public List<Process> resizes = new ArrayList<Process>();
    public LinkedList<Agent> onStage = new LinkedList<Agent>();

    public Page(){
        initialize();
    }

    /**
     * Insantiates all variables in the page
     */
    public abstract void initialize();

    public abstract void restart();

    public void addTimer(Timer t){
        timers.add(t);
        t.enabled = true;
    }

    public void addToStage(Agent a){
        Manager.addToStage(a);
        onStage.add(a);
        a.parent = this;
        a.frame();
    }

    public void addToStage(Agent... agents){
        for(Agent a : agents){
            Manager.addToStage(a);
            onStage.add(a);
            a.parent = this;
            a.frame();
        };
    }

    public void removeFromStage(Agent... agents){
        for(Agent a : agents){
            if(onStage.contains(a)){
                a.remove();
                onStage.remove(a);
            }
        }
    }

    public void removeFromStage(Agent a){
        if(onStage.contains(a)){
            a.remove();
            onStage.remove(a);
        }
    }

    public void add(Agent a){
        renderer.addObject(a);
        a.parent = this;
        a.frame();
    }

    public void add(Graphic s){
        renderer.addObject(s);
    }

    public void remove(Agent a){
        renderer.removeObject(a);
    }

    public void remove(Graphic s){
        renderer.removeObject(s);
    }

    public void removeAgentsFromStage(){
        ListIterator<Agent> iter = onStage.listIterator();
        while(iter.hasNext()){
            Agent a = iter.next();
            iter.remove();
            a.remove();
        }
    }

    public void addAnimation(Animation a){
        animations.add(a);
    }

    protected void finish(){
        for(int i = 0; i < timers.size(); i++){
            timers.remove(0).stop();
        }
        for(int i = 0; i < animations.size(); i++){
            animations.remove(0).stop();
        }
    }

    public void addResize(Process p){
        resizes.add(p);
    }

    public void removeResize(Process p){
        resizes.remove(p);
    }

    public abstract void dispose();

    /**
     * Removes all Agents and Entities from the page
     */
    public void delete(){
        dispose();
        finish();
    }
}
