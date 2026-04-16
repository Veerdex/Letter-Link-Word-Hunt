package com.grantkoupal.letterlink.quantum.ui;

import com.grantkoupal.letterlink.Source;
import com.grantkoupal.letterlink.quantum.core.Manager;

import java.util.ArrayList;
import java.util.List;

public class InputButton{
    public enum ButtonType {RECT, CIRC};
    private static final List<InputButton> buttonList = new ArrayList<>();
    private float anchorX = 0;
    private float anchorY = 0;
    private float x = 0;
    private float y = 0;
    private float width = 0;
    private float height = 0;
    private final ButtonType type;
    private float radius = 0;
    private float scalar = 1;
    private boolean isHover = false;
    private boolean clicked = false;

    public static void updateButtons(){
        for(int i = 0; i < buttonList.size(); i++){
            buttonList.get(i).update();
        }
    }

    public InputButton(float x, float y, float radius){
        this.x = x;
        this.y = y;
        this.radius = radius;
        type = ButtonType.CIRC;
        buttonList.add(this);
    }

    public InputButton(float x, float y, float width, float height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        type = ButtonType.RECT;
        buttonList.add(this);
    }

    public void deactivate(){
        buttonList.remove(this);
    }

    public void setRadius(float r){
        radius = r;
    }

    public float getRadius(){
        return radius;
    }

    public void setScalar(float s){
        scalar = s;
    }

    public float getScalar(){
        return scalar;
    }

    public boolean isClicked(){
        return clicked;
    }

    public boolean isHover(){
        return isHover;
    }

    public void setAnchor(float ratioX, float ratioY){
        anchorX = ratioX;
        anchorY = ratioY;
    }

    public void setAnchorX(float x){
        anchorX = x;
    }

    public float getAnchorX(){
        return anchorX;
    }

    public void setAnchorY(float y){
        anchorY = y;
    }

    public float getAnchorY(){
        return anchorY;
    }

    public void setXY(float x, float y){
        this.x = x;
        this.y = y;
    }

    public void setX(float x){
        this.x = x;
    }

    public float getX(){
        return x;
    }

    public void setY(float y){
        this.y = y;
    }

    public float getY(){
        return y;
    }

    private void update(){
        if(getIsHover()){
            isHover = true;
            clicked = Manager.isClick();
        } else {
            isHover = false;
        }
    }

    private boolean getIsHover(){
        float scale = Manager.getScale();
        if(ButtonType.RECT == type)
            return Math.abs(Source.getMouseX() - (Source.getScreenWidth() * anchorX) + x * scale) < width / 2 * scale * scalar &&
                Math.abs(Source.getMouseY() - (Source.getScreenHeight() * anchorY) + y * scale) < height / 2 * scale * scalar;

        return Math.sqrt(Math.pow(Source.getMouseX() - (Source.getScreenWidth() * anchorX) + x * scale, 2) +
            Math.pow(Source.getMouseY() - (Source.getScreenHeight() * anchorY) + y * scale, 2)) < radius * scale * scalar;
    }
}
