package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.grantkoupal.letterlink.quantum.core.Page;
import com.grantkoupal.letterlink.quantum.core.TimeFrame;
import com.grantkoupal.letterlink.quantum.core.Timer;

public class LoadingBoardPage extends Page {

    private LoadingAnimation spinner;
    private volatile boolean loadingComplete = false;
    private Thread loadingThread;

    @Override
    public void initialize(){
        // Create spinner at center of screen
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        spinner = new LoadingAnimation(centerX, centerY, this);
        add(spinner);

        // Start loading board in background thread
        loadingThread = new Thread() {
            @Override
            public void run() {
                try {
                    // This is where the heavy loading happens
                    ImprovedBoardGenerator.generateBoard(4, 5, 5);

                    // Mark as complete
                    loadingComplete = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        loadingThread.setDaemon(true);
        loadingThread.start();

        addTimer(new Timer(.25f, Timer.INDEFINITE, new TimeFrame(){
            @Override
            public void run(long iteration) {
                if(loadingComplete){
                    stop();
                    PracticePage practicePage = new PracticePage();

                    Source.loadNewPage(practicePage);
                }
            }
        }));
    }

    @Override
    public void restart() {
        // Reset loading state if needed
        loadingComplete = false;

        // Restart the loading process
        initialize();
    }

    @Override
    public void dispose() {
        if (spinner != null) {
            spinner.dispose();
        }

        // Make sure loading thread is finished
        if (loadingThread != null && loadingThread.isAlive()) {
            loadingThread.interrupt();
            try {
                loadingThread.join(1000); // Wait up to 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
