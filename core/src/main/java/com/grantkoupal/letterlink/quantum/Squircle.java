package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Squircle extends Agent{

    private float width = 10;
    private float height = 10;
    private float x  = 0;
    private float y = 0;
    private float curve = 5;

    public static void drawSquircle(ShapeRenderer sr, float x, float y, float width, float height, float curve){
        sr.circle(x - width / 2 + curve, y - height / 2 + curve, curve, 25);
        sr.circle(x + width / 2 - curve, y - height / 2 + curve, curve, 25);
        sr.circle(x + width / 2 - curve, y + height / 2 - curve, curve, 25);
        sr.circle(x - width / 2 + curve, y + height / 2 - curve, curve, 25);
        sr.rect(x - width / 2 + curve, y - height / 2, width - curve * 2, height);
        sr.rect(x - width / 2, y - height / 2 + curve, width, height - curve * 2);
    }

    public static void drawSquircleWithOutline(ShapeRenderer sr, Color fill, Color outline, float thickness,
                                               float x, float y, float width, float height, float curve){
        sr.setColor(outline);
        drawSquircle(sr, x, y, width, height, curve);
        sr.setColor(fill);
        float innerCurve = Math.min((width - thickness * 2) / width, (height - thickness * 2) / height);
        drawSquircle(sr, x, y, width - thickness * 2, height - thickness * 2, curve * innerCurve);
    }

    public Squircle(float x, float y, float width, float height, float curve){
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.curve = curve;
    }

    public void setCurve(float f){
        curve = f;
    }

    public void setWidth(float f){
        width = f;
    }

    public void setHeight(float f){
        height = f;
    }

    public void setCenterX(float f){
        x = f;
    }

    public void setCenterY(float f){
        y = f;
    }

    public float getCurve(){
        return curve;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    public float getCenterX(){
        return x;
    }

    public float getCenterY(){
        return y;
    }

        @Override
    public void dispose() {}

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        sr.rect(x - width / 2 + curve, y - height / 2, width - curve * 2, height);
        sr.rect(x - width / 2, y - height / 2 + curve, width, height - curve * 2);
        sr.circle(x - width / 2 + curve, y - height / 2 + curve, curve, 25);
        sr.circle(x + width / 2 - curve, y - height / 2 + curve, curve, 25);
        sr.circle(x + width / 2 - curve, y + height / 2 - curve, curve, 25);
        sr.circle(x - width / 2 + curve, y + height / 2 - curve, curve, 25);
    }
}
