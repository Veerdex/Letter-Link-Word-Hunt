package com.grantkoupal.letterlink.quantum.movement;

import com.badlogic.gdx.Gdx;

import java.util.List;

public class Oscillator {

    public static enum Mode{Linear, Smooth, Momentum};

    public static int posX = 0;
    public static int posY = 1;
    public static int scalX = 2;
    public static int scalY = 3;
    public static int rot = 4;

    /**
     * Position X, PositionY, Scale X, ScaleY, Rotation
     */
    private Mode transitionType = Mode.Linear;
    private float duration = 1;
    private float[] transform = new float[]{0, 0, 1, 1, 0};
    private float[] prevState;
    private float[] shift;
    private List<float[]> states;
    private float transitionTime;
    private int currentIndex = 0;

    public Oscillator(List<float[]> states){
        this.states = states;
        for(int i = 0; i < states.get(0).length; i++){
            transform[i] = states.get(0)[i];
        }
        prevState = transform;
        shift();
    }

    public float[] getNextState(){
        if(currentIndex == states.size() - 1){
            return clone(states.get(0));
        }
        return clone(states.get(currentIndex + 1));
    }

    public float[] nextState(){
        prevState = states.get(currentIndex);
        if(currentIndex == states.size() - 1){
            currentIndex = 0;
        } else {
            currentIndex++;
        }
        return clone(states.get(currentIndex));
    }

    public void setDuration(float d){
        duration = d;

    }

    public float getDuration(){
        return duration;
    }

    public void shift(){
        transitionTime = 0;
        shift = subtract(nextState(), prevState);
    }

    public void update(float delta){
        transitionTime += delta;
        if(transitionType == Mode.Linear){
            add(transform, multiply(shift, delta / duration));
        }
        if(transitionTime >= duration){
            shift();
        }
    }

    public void setMode(Mode m){
        transitionType = m;
    }

    public float[] subtract(float[] a, float[] b){
        int loops;
        if(a.length <= b.length){
            loops = a.length;
        } else {
            loops = b.length;
        }

        for(int i = 0; i < loops; i++){
            a[i] -= b[i];
        }

        return a;
    }

    public float[] divide(float[] a, float b){
        for(int i = 0; i < a.length; i++){
            a[i] /= b;
        }

        return a;
    }

    public float[] multiply(float[] a, float b){

        float[] output = clone(a);

        for(int i = 0; i < a.length; i++){
            output[i] *= b;
        }

        return output;
    }

    private float[] clone(float[] a){
        float[] copy = new float[a.length];
        System.arraycopy(a, 0, copy, 0, a.length);
        return copy;
    }

    public float[] add(float[] a, float[] b){
        int loops;
        if(a.length <= b.length){
            loops = a.length;
        } else {
            loops = b.length;
        }

        for(int i = 0; i < loops; i++){
            a[i] += b[i];
            Gdx.app.log("Oscillator", "Added: " + b[1]);
        }

        return a;
    }

    public void setValue(int index, float value){
        transform[index] = value;
    }

    public float getValue(int index){
        return transform[index];
    }

    public float[] getTransform(){
        return transform;
    }

    public float getPosX(){
        return transform[0];
    }

    public float getPosY(){
        return transform[1];
    }

    public float getScalX(){
        return transform[2];
    }

    public float getScalY(){
        return transform[3];
    }

    public float getRot(){
        return transform[4];
    }
}
