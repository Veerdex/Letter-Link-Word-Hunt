package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class Agent extends Actor implements Disposable{
    protected boolean isEntity = false;
    private int viewOrder = 0;
    public Renderer parentRenderer = null;

    protected Page parent;

    /**
     * Actions performed once the Agent is added to a stage
     */
    public void frame(){}

    public abstract void dispose();

    public abstract void draw(ShapeRenderer sr, SpriteBatch sb);

    public void setPage(Page p){
        parent = p;
    }

    public Page getPage(){
        return parent;
    }

    public void delete(){
        dispose();
    }

    public boolean isEntity(){
        return isEntity;
    }

    public void setViewOrder(int i){
        viewOrder = i;
        parentRenderer.removeObject(this);
        parentRenderer.addObject(this);
    }

    public int getViewOrder(){
        return viewOrder;
    }
}
