package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Graphic extends Sprite {
    private int viewOrder = 0;
    public Renderer parentRenderer = null;

    public Graphic(Texture t){
        super(t);
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
