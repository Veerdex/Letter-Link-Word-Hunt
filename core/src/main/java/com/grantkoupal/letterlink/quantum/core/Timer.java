package com.grantkoupal.letterlink.quantum.core;

public class Timer{

    public static final int INDEFINITE = -1;
    private boolean indefinite = false;
    private int iterations;
    private final float step;
    private float timePassed = 0;
    private boolean isFinished = false;
    public boolean enabled = false;
    private TimeFrame timeFrame;
    private int passes;
    private Runnable onEnd;

    public Timer(float step, int iterations, TimeFrame timeFrame){
        if(iterations == -1){
            indefinite = true;
        }
        this.iterations = iterations;
        this.step = step;
        this.timeFrame = timeFrame;
        this.timeFrame.parentTimer = this;
    }

    public void stop(){
        if(enabled && onEnd != null){
            onEnd.run();
            enabled = false;
        }
        isFinished = true;
    }

    public boolean isFinished(){
        return isFinished;
    }

    public boolean update(float deltaTime){
        if(isFinished || (iterations <= 0 && !indefinite)){
            stop();
            return false;
        }
        timePassed += deltaTime / step;
        int loops = (int)timePassed;
        timePassed %= 1.0f;
        iterations -= loops;
        for(int i = 0; i < loops; i++){
            passes++;
            timeFrame.run(passes);
        }

        return true;
    }

    public int getIteration(){
        return passes;
    }

    public void onEnd(Runnable action){
        onEnd = action;
    }
}
