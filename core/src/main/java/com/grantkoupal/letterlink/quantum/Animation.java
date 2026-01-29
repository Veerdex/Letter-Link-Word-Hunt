package com.grantkoupal.letterlink.quantum;

public class Animation{

    public static final int INDEFINITE = -1;
    private float cutoff = -1;
    private boolean isFinished = false;
    private Action action;
    private Runnable onEnd;

    /**
     * Gives the ability to run processes on each frame
     * @param nanoTime Time when the animation was created
     * @param duration Total time the program will run
     * @param action The process which will be performed on each frame
     */
    public Animation(long nanoTime, long duration, Action action){
        if(duration != -1){
            this.cutoff = nanoTime + duration * 1000000f;
        }

        this.action = action;
        this.action.parentAnimation = this;
    }

    /**
     * Stops the program and if the user states onEnd() it will run it
     */
    public void stop(){
        if(onEnd != null){
            onEnd.run();
        }
        isFinished = true;
    }

    public boolean isFinished(){
        return isFinished;
    }

    /**
     * Process provided by the user which runs every frame, at its last frame it makes sure to not go over the total time its supposed to run
     *
     * @param nanoTime Total time the program has been running in nano seconds
     * @param deltaTime Time since last loaded from in seconds
     * @return Returns false to indicate to the Manager to remove the process, true otherwise
     */
    public boolean update(long nanoTime, float deltaTime){
        if(isFinished){
            return false;
        }

        if(cutoff != -1 && nanoTime + deltaTime * 1000000000f > cutoff){
            action.run((cutoff - nanoTime) / 1000000000f);
            stop();
            return false;
        } else{
            action.run(deltaTime);
        }

        return true;
    }

    /**
     * Sets the process which will be run when the animation ends
     * @param action The process
     */
    public void onEnd(Runnable action){
        onEnd = action;
    }
}
