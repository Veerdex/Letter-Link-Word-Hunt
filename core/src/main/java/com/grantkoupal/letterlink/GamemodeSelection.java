package com.grantkoupal.letterlink;

import com.grantkoupal.letterlink.quantum.core.Page;
import com.grantkoupal.letterlink.quantum.core.TimeFrame;
import com.grantkoupal.letterlink.quantum.core.Timer;

public class GamemodeSelection extends Page {

    public GamemodeSelection(){
        DataManager.currentLayout = DataManager.BoardLayout._4x4;
        addTimer(new Timer(1f, 1, new TimeFrame(){
            @Override
            public void run(long iteration) {
                Source.previousPage();
            }
        }));

    }

    @Override
    public void initialize() {

    }

    @Override
    public void restart() {

    }

    @Override
    public void dispose() {

    }
}
