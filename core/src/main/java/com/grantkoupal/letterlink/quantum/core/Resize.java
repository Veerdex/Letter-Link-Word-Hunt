package com.grantkoupal.letterlink.quantum.core;
public class Resize {
    private boolean active = true;
    private final Resizable action;

    public Resize(Resizable action){
        this.action = action;
    }

    public boolean resize(){
        action.run();
        return active;
    }

    public void stop(){
        active = false;
    }
}
