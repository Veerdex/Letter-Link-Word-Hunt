package com.grantkoupal.letterlink;

import com.grantkoupal.letterlink.quantum.Page;
import com.grantkoupal.letterlink.quantum.TimeFrame;
import com.grantkoupal.letterlink.quantum.Timer;

public class LoadingBoardPage extends Page {
    @Override
    public void initialize() {
        /*Thread hi = new Thread(){
            @Override
            public void run(){
                while(!Thread.currentThread().isInterrupted()) {
                    System.out.println("HI");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie){
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        };
        hi.setDaemon(true);
        hi.start();
        Board board = new Board(4, 4, 4);
        hi.interrupt();*/
        Timer t = new Timer(1f, 1, new TimeFrame(){
            @Override
            public void run(long iteration) {
                System.out.println("HI");
                PracticePage practicePage = new PracticePage();
                Source.loadNewPage(practicePage);
            }
        });
        addTimer(t);

        //PracticePage practicePage = new PracticePage();
        //Source.loadPage(practicePage);
    }

    @Override
    public void restart() {

    }

    @Override
    public void dispose() {

    }
}
