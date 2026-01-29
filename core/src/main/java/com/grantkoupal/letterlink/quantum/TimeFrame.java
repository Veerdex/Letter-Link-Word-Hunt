package com.grantkoupal.letterlink.quantum;

public abstract class TimeFrame{
    public Timer parentTimer = null;

    /**
     * The process which the parentTimer will run
     * @param delta
     */
    public abstract void run(long iteration);

    /**
     * Stops the program for the parentTimer
     */
    protected void stop(){
        parentTimer.stop();
    }
}
