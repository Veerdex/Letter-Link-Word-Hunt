package com.grantkoupal.letterlink.quantum.core;

public abstract class Resizable {
    public Resize parent;

    public abstract void run();

    public void stop(){
        parent.stop();
    }
}
