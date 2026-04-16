package com.grantkoupal.letterlink.quantum.core;

public class Timer{

    public static final int INDEFINITE = -1;
    private boolean indefinite = false;
    private int iterations;
    private final float step;
    private float timePassed = 0;
    private boolean isFinished = false;
    private TimeFrame timeFrame;
    private int passes;
    public boolean isActive = false;
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

    protected void setUp(){
        isFinished = false;
        iterations = 0;
    }

    public void restart(){
        setUp();
    }

    public void stop(){
        if(onEnd != null){
            onEnd.run();
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
