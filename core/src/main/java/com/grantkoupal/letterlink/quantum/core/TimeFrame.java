package com.grantkoupal.letterlink.quantum.core;

public abstract class TimeFrame{
    public Timer parentTimer = null;

    /**
     * The process which the parentTimer will run
     * @param iteration
     */
    public abstract void run(long iteration);

    /**
     * Stops the program for the parentTimer
     */
    protected void stop(){
        parentTimer.stop();
    }
}
