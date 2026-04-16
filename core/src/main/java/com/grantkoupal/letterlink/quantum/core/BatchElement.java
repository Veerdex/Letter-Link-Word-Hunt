package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BatchElement{

    private float xOffset = 0;
    private float yOffset = 0;
    private float scaleOffset = 1;
    private final SpriteBatch sb;

    public BatchElement(SpriteBatch sb){
        this.sb = sb;
    }

    public void setX(float x){
        xOffset = x;
    }

    public void setY(float y){
        yOffset = y;
    }

    public void setScale(float scale){
        scaleOffset = scale;
    }

    public float getX(){
        return xOffset;
    }

    public float getY(){
        return yOffset;
    }

    public float getScale(){
        return scaleOffset;
    }

    public float convertX(float x){
        return (x + xOffset) * scaleOffset;
    }

    public float convertY(float y){
        return (y + yOffset) * scaleOffset;
    }

    public void draw(Texture texture, float x, float y){
        sb.draw(texture, convertX(x), convertY(y), texture.getWidth() * scaleOffset, texture.getHeight() * scaleOffset);
    }

    public void draw(Texture texture, float x, float y, float width, float height){
        sb.draw(texture, convertX(x), convertY(y), width * scaleOffset, height * scaleOffset);
    }
}
