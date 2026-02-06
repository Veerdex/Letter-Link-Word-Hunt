package com.grantkoupal.letterlink.quantum.core;

public abstract class Action{
    public Animation parentAnimation = null;

    public abstract void run(float delta);

    protected void stop(){
        parentAnimation.stop();
    }
}
