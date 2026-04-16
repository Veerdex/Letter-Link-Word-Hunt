package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ShapeElement{

    private float xOffset = 0;
    private float yOffset = 0;
    private float scaleOffset = 1;
    private final ShapeRenderer sr;

    public ShapeElement(ShapeRenderer sr){
        this.sr = sr;
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

    public void rectLine(float x1, float y1, float x2, float y2, float width){
        sr.rectLine(convertX(x1), convertY(y1), convertX(x2), convertY(y2), width * scaleOffset);
    }

    public void circle(float x, float y, float radius){
        sr.circle(convertX(x), convertY(y), radius * scaleOffset);
    }

    public void rect(float x, float y, float width, float height){
        sr.rect(convertX(x), convertY(y), width * scaleOffset, height * scaleOffset);
    }
}
