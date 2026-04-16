package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.grantkoupal.letterlink.backend.BackendHandler;
import com.grantkoupal.letterlink.backend.MatchStatusResponse;
import com.grantkoupal.letterlink.backend.QueueTicketResponse;
import com.grantkoupal.letterlink.backend.data.SessionData;
import com.grantkoupal.letterlink.quantum.core.Page;
import com.grantkoupal.letterlink.quantum.core.TimeFrame;
import com.grantkoupal.letterlink.quantum.core.Timer;

import java.util.ArrayList;

public class FindMatch extends Page {

    // ===== Constants =====
    private static final float POLL_INTERVAL_SECONDS = 0.25f;

    // ===== UI =====
    private LoadingAnimation spinner;

    // ===== Loading state =====
    private volatile boolean searchingComplete = false;
    private volatile boolean matchFound = false;
    private Thread loadingThread;

    @Override
    public void initialize() {
        createSpinner();
    }

    @Override
    public void frame(){
        startFindingMatch();
        startCompletionPollTimer();
    }

    private void createSpinner() {
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        spinner = new LoadingAnimation(centerX, centerY, this);
        add(spinner);
    }

    private void startFindingMatch() {
        BackendHandler.startMatchmaking(
            SessionData.mode,
            SessionData.currentGamemode,
            SessionData.currentBoardWidth,
            SessionData.currentBoardHeight,
            new BackendHandler.MatchmakingCallback() {
                @Override
                public void onQueued(QueueTicketResponse response) {
                    System.out.println("Queued.");
                    System.out.println("Ticket ID: " + response.ticketId);
                    System.out.println("Mode: " + response.mode);
                    System.out.println("Gamemode: " + response.currentGamemode);
                    System.out.println("Board: " + response.boardWidth + "x" + response.boardHeight);
                    System.out.println("MMR used: " + response.mmr);
                }

                @Override
                public void onMatchFound(MatchStatusResponse response) {
                    System.out.println("MATCH FOUND");
                }

                @Override
                public void onMatchReady(MatchStatusResponse response) {
                    System.out.println("MATCH READY");
                    Solver.setBoard(SessionData.currentBoardWidth, SessionData.currentBoardHeight, response.boardLetters);
                    Solver.resetWords();
                    int width = SessionData.currentBoardWidth;
                    int height = SessionData.currentBoardHeight;

                    // Find all valid words in the board
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            Solver.checkWords(x, y, "", new int[width][height], new ArrayList<>());
                        }
                    }

                    Solver.setBoardValue(Solver.calculatePoints());
                    Solver.organize();

                    matchFound = true;
                    searchingComplete = true;
                }

                @Override
                public void onCancelled() {
                    System.out.println("CANCELLED");
                    searchingComplete = true;
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            }
        );
    }

    private void startCompletionPollTimer() {
        add(new Timer(POLL_INTERVAL_SECONDS, Timer.INDEFINITE, new TimeFrame() {
            @Override
            public void run(long iteration) {
                if (!searchingComplete) return;

                stop();
                Source.loadNewPage(new GamePage());
            }
        }));
    }

    @Override
    public void restart() {
        // Reset loading state if needed
        searchingComplete = false;

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
