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

    // ===== Game Components =====
    private Board board;

    private HintTable hintTable;
    private GuessTable guessTable;
    private ChainDisplay chainDisplay;
    private PointsDisplay pointsDisplay;
    private MenuDisplay menuDisplay;

    private PracticeMenu practiceMenu;
    private Settings settings;

    @Override
    public void initialize() {
        initBoard();
        add(board);

        computeAndSetSRankScore();

        createUIComponents();
        setupResizeHandler();
    }

    // ===== Initialization helpers =====

    private void initBoard() {
        board = new Board();
        Board.loadNewBoard();

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
    }

    /**
     * Calculates a target score (S-rank score) based on a subset of easier words.
     * Original behavior preserved (same math + loop structure).
     */
    private void computeAndSetSRankScore() {
        List<String> words = Board.getWordsInBoard();

        int total;
        int x = words.size();

        if (x < 1149.62325) {
            int aValue = (int) (-20 * (Math.log(x) / Math.log(5)) + 100);
            total = (int) (aValue * x / 178.57527f);
        } else {
            total = 80;
        }

        float predictedScore = 0;
        float totalPossibleScore = 0; // kept (even if unused) to preserve structure/intent
        int count = 0;

        main:
        for (int a = 0; a < 20; a++) {
            for (int i = a; i < words.size(); i += 20) {
                if (count == total) break main;

                String word = words.get(i);

                // Only count easier words in predicted score
                if (WordDifficultyRanker.wordDifficulty(word) < 80) {
                    count++;
                    predictedScore += Solver.getWordValue(word);
                }
            }
        }

        for (String word : words) {
            totalPossibleScore += Solver.getWordValue(word);
        }

        board.setSRankScore((int) predictedScore);
    }

    private void createUIComponents() {
        // Left side: all possible words (hidden until found)
        hintTable = new HintTable(board.getWordsInBoard(), board.getWordsFound());
        add(hintTable);

        // Right side: words the player has found
        guessTable = new GuessTable(board.getListOfWordsFound());
        add(guessTable);

        // Current letter chain being traced
        chainDisplay = new ChainDisplay();
        add(chainDisplay);

        // Score display
        pointsDisplay = new PointsDisplay();
        add(pointsDisplay);

        // Top menu / HUD
        menuDisplay = new MenuDisplay();
        add(menuDisplay);

        // Pause/menu overlay
        practiceMenu = new PracticeMenu();
        add(practiceMenu);

        // Settings overlay
        settings = new Settings();
        add(settings);
    }

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

    // ===== Page Lifecycle =====

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
        practiceMenu.dispose();
        settings.dispose();
    }
}
