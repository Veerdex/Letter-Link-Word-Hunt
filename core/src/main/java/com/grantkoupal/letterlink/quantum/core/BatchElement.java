package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.grantkoupal.letterlink.Source;

public class BatchElement{

    private float xOffset = 0;
    private float yOffset = 0;
    private float ratioX = 0;
    private float ratioY = 0;
    private float scaleOffset = 1;
    private SpriteBatch sb;

    public BatchElement(){}

    public BatchElement(SpriteBatch sb){
        this.sb = sb;
    }

    public void setSpriteBatch(SpriteBatch sb){
        this.sb = sb;
    }

    public void setX(float x){
        xOffset = x;
    }

    public void setY(float y){
        yOffset = y;
    }

    public void setRatioX(float x){
        ratioX = x;
    }

    public void setRatioY(float y){
        ratioY = y;
    }

    public void setScale(float scale){
        scaleOffset = scale;
    }

    public float getRatioX(){
        return ratioX;
    }

    public float getRatioY(){
        return ratioY;
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
        return (x * Source.getScale() + xOffset) * scaleOffset + ratioX * Source.getScreenWidth();
    }

    public float convertY(float y){
        return (y * Source.getScale() + yOffset) * scaleOffset + ratioY * Source.getScreenHeight();
    }

    public void draw(Texture texture, float x, float y){
        sb.draw(texture, (convertX(x) - texture.getWidth() / 2f * Source.getScale()) * scaleOffset,
            (convertY(y) - texture.getHeight() / 2f * Source.getScale()) * scaleOffset,
            texture.getWidth() * scaleOffset * Source.getScale(),
            texture.getHeight() * scaleOffset * Source.getScale());
    }

    public void draw(Texture texture, float x, float y, float width, float height){
        sb.draw(texture, (convertX(x) - texture.getWidth() / 2f * Source.getScale()) * scaleOffset,
            (convertY(y) - texture.getHeight() / 2f * Source.getScale()) * scaleOffset,
            width * scaleOffset * Source.getScale(),
            height * scaleOffset * Source.getScale());
    }
}
