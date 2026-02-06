package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;

import java.util.LinkedList;
import java.util.List;

/**
 * Supports ShapeRenderer, SpriteBatch
 */
public class Renderer extends Actor implements Disposable{
    private ShapeRenderer SR;
    private SpriteBatch SB;

    protected List<Agent> agents = new LinkedList<Agent>();
    protected List<Graphic> graphics = new LinkedList<Graphic>();

    public Renderer(){}

    public Renderer(ShapeRenderer sr, SpriteBatch sb){
        SR = sr;
        SB = sb;
    }

    public void setShapeRenderer(ShapeRenderer sr){
        SR = sr;
    }

    public void setSpriteBatch(SpriteBatch sb){
        SB = sb;
    }

    public void addObject(Agent a){
        a.parentRenderer = this;
        int i;
        for(i = 0; i < agents.size(); i++){
            if(agents.get(i).getViewOrder() > a.getViewOrder()){
                agents.add(i, a);
                return;
            }
        }
        agents.add(a);
    }

    public void addObject(Graphic g){
        g.parentRenderer = this;
        int i;
        for(i = 0; i < graphics.size(); i++){
            if(graphics.get(i).getViewOrder() > g.getViewOrder()){
                graphics.add(i, g);
                return;
            }
        }
        graphics.add(g);
    }

    public void removeObject(Agent a){
        agents.remove(a);
    }

    public void removeObject(Graphic g){
        graphics.remove(g);
    }

    @Override
    public void dispose() {
        agents.clear();
        graphics.clear();
        if(SR != null){
            SR.dispose();
        }
        if(SB != null){
            SB.dispose();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        SB.setProjectionMatrix(Manager.camera.combined);
        SR.setProjectionMatrix(Manager.camera.combined);
        SB.begin();
        int k = 0;
        for(int i = 0; i < graphics.size(); i++){
            int spriteViewOrder = graphics.get(i).getViewOrder();
            while(k < agents.size()){
                if(agents.get(k).getViewOrder() < spriteViewOrder){
                    agents.get(k).draw(SR, SB);
                } else {
                    k++;
                    break;
                }
                k++;
            }
            graphics.get(i).draw(SB);
        }
        SB.end();

        while(k < agents.size()){
            agents.get(k).draw(SR, SB);
            k++;
        }
    }
}
