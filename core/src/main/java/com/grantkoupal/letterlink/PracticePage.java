package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.grantkoupal.letterlink.quantum.Page;
import com.grantkoupal.letterlink.quantum.Process;

import java.util.List;

public class PracticePage extends Page {

    private Board board;
    private HintTable hintTable;
    private GuessTable guessTable;
    private ChainDisplay chainDisplay;
    private PointsDisplay pointsDisplay;
    private final int boardWidth = 4;
    private final int boardHeight = 4;
    private BitmapFont font;

    @Override
    public void initialize() {

        DataManager.setAssets("Wood Piece.png", "Boise.png", "Waves.png", "SuperSense");

        long startTime = System.nanoTime();

        /*
        4x4 (Each test was run for 3 minutes or 100 iterations)
        Power 0 -> Seconds: .02     Points: 84,490      Rating: 5,052,045
        Power 1 -> Seconds: .08     Points: 112,622     Rating: 1,350,481
        Power 2 -> Seconds: .2      Points: 175,489     Rating: 837,926
        Power 3 -> Seconds: .56     Points: 281,323     Rating: 503,596
        Power 4 -> Seconds: 1.07    Points: 382,028     Rating: 355,273
        Power 5 -> Seconds: 12.11   Points: 786,881     Rating: 64,958
        Power 6 -> Seconds: 20.94   Points: 929,511     Rating: 44,383
        Power 7 -> Seconds: 46.97   Points: 893,960     Rating: 19,030
        Power 8 -> Seconds: 32.16   Points: 882,833     Rating: 27,451
        Power 9 -> Seconds: 25.84   Points: 977,557     Rating: 37,828
         */
        board = new Board(boardWidth, boardHeight, 4);
        board.addAnimations(this);

        List<String> words = board.getWordsInBoard();

        float predictedScore = 0;

        for(int i = 0; i < words.size(); i++){
            if(WordDifficultyRanker.wordDifficulty(words.get(i)) < 20){
                predictedScore += Solver.getWordValue(words.get(i));
            }
        }

        System.out.println("Predicted Score: " + predictedScore);

        add(board);

        hintTable = new HintTable(board.getWordsInBoard(), board.getWordsFound());

        add(hintTable);

        guessTable = new GuessTable(board.getListOfWordsFound());

        add(guessTable);

        chainDisplay = new ChainDisplay(board);

        add(chainDisplay);

        pointsDisplay = new PointsDisplay(board, this);

        add(pointsDisplay);

        Source.addRenderer(renderer);

        run();
    }

    private void run(){
        addResize(new Process() {
            @Override
            public boolean run() {
                board.updateFrameBuffer(new FrameBuffer(Pixmap.Format.RGBA8888, Source.getScreenWidth(), Source.getScreenHeight(), false));
                return true;
            }
        });
    }

    @Override
    public void restart() {}

    @Override
    public void dispose() {
        board.dispose();
        DataManager.dispose();
        guessTable.dispose();
        hintTable.dispose();
    }
}
