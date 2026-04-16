package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Collections;
import java.util.LinkedList;

public class Group extends Agent{

    private float scale = 1;
    //private
    private final LinkedList<Agent> agents = new LinkedList<>();

    public Group(){}

    public Group(Agent... agents){
        Collections.addAll(this.agents, agents);
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        for(Agent a : agents){

        }
    }

    @Override
    public void dispose() {

    }

    //interface
}
