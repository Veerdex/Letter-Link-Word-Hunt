package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.grantkoupal.letterlink.quantum.core.Page;
import com.grantkoupal.letterlink.quantum.core.Process;

import java.util.List;

/**
 * The main practice game page where players find words on a letter grid.
 * Manages the game board, hint table, guess display, and scoring.
 */
public class PracticePage extends Page {

    // ========== Constants ==========

    // ========== Game Components ==========
    private Board board;
    private HintTable hintTable;
    private GuessTable guessTable;
    private ChainDisplay chainDisplay;
    private PointsDisplay pointsDisplay;
    private MenuDisplay menuDisplay;

    // ========== Initialization ==========

    @Override
    public void initialize() {
        board = new Board(Solver.getBoardWidth(), Solver.getBoardHeight(), Solver.getBoard());

        /*
        Board Generation Performance (4x4 grid, 3 min or 100 iterations):
        Power 0 -> Time: .02s   Points: 84,490    Rating: 5,052,045
        Power 1 -> Time: .08s   Points: 112,622   Rating: 1,350,481
        Power 2 -> Time: .2s    Points: 175,489   Rating: 837,926
        Power 3 -> Time: .56s   Points: 281,323   Rating: 503,596
        Power 4 -> Time: 1.07s  Points: 382,028   Rating: 355,273
        Power 5 -> Time: 12.11s Points: 786,881   Rating: 64,958
        Power 6 -> Time: 20.94s Points: 929,511   Rating: 44,383
        Power 7 -> Time: 46.97s Points: 893,960   Rating: 19,030
        Power 8 -> Time: 32.16s Points: 882,833   Rating: 27,451
        Power 9 -> Time: 25.84s Points: 977,557   Rating: 37,828
        */

        board.addAnimations(this);
        add(board);

        logBoardStatistics();

        createUIComponents();
        setupResizeHandler();
    }

    /**
     * Calculates and logs predicted score based on word difficulty.
     * Only counts words with difficulty < 20 as "findable".
     */
    private void logBoardStatistics() {
        List<String> words = board.getWordsInBoard();

        System.out.println(words);

        float predictedScore = 0;
        float totalPossibleScore = 0;

        for (String word : words) {
            int wordValue = Solver.getWordValue(word);
            totalPossibleScore += wordValue;

            // Only count easier words in predicted score
            if (WordDifficultyRanker.wordDifficulty(word) < 25) {
                predictedScore += wordValue;
            }
        }

        System.out.println("Predicted Score(" + (int)Math.pow(25, 1.25) + "): " + predictedScore);
        System.out.println("Total Possible: " + totalPossibleScore);
    }

    /**
     * Creates and adds all UI components (tables, displays).
     */
    private void createUIComponents() {
        // Left side: shows all possible words (hidden until found)
        hintTable = new HintTable(board.getWordsInBoard(), board.getWordsFound());
        add(hintTable);

        // Right side: shows words player has found
        guessTable = new GuessTable(board.getListOfWordsFound());
        add(guessTable);

        // Displays current letter chain being traced
        chainDisplay = new ChainDisplay(board);
        add(chainDisplay);

        // Shows current score and points
        pointsDisplay = new PointsDisplay(board, this);
        add(pointsDisplay);

        // Displays icon, time left, and rank
        menuDisplay = new MenuDisplay(board);
        add(menuDisplay);
    }

    /**
     * Sets up handler to recreate board framebuffer on window resize.
     */
    private void setupResizeHandler() {
        addResize(new Process() {
            @Override
            public boolean run() {
                FrameBuffer newFb = new FrameBuffer(
                    Pixmap.Format.RGBA8888,
                    Source.getScreenWidth(),
                    Source.getScreenHeight(),
                    false
                );
                board.updateFrameBuffer(newFb);
                return true;
            }
        });
    }

    // ========== Page Lifecycle ==========

    @Override
    public void restart() {
        // Currently not implemented - would reset the game state
    }

    @Override
    public void dispose() {
        board.dispose();
        guessTable.dispose();
        hintTable.dispose();
        chainDisplay.dispose();
        pointsDisplay.dispose();
        menuDisplay.dispose();
        DataManager.dispose();
    }
}
