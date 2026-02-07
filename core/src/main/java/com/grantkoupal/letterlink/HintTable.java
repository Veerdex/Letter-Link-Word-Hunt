package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.Action;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.Animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Displays a scrollable table of all valid words in the puzzle.
 * Words appear as question marks until found, then show in gold.
 * Renders on the left side of the screen with mouse scroll support.
 */
public class HintTable extends Agent {

    // ========== Constants ==========
    private static final int VISIBLE_ROWS = 8;
    private static final String HIDDEN_WORD_PLACEHOLDER = "??????????????????????????????";

    // ========== Graphics ==========
    private BitmapFont font;
    private GlyphLayout fontLayout;
    private final int fontSize = 32;

    // ========== Data ==========
    private List<String> validWords;
    private List<Boolean> wordsFound;

    // ========== Scroll State ==========
    private float scroll = 0;
    private float scrollMotion = 0;

    // ========== Layout ==========
    private float scale = 1;
    private float hintX = 0;
    private float hintY = 0;

    // ========== Constructor ==========

    /**
     * Creates a new HintTable showing all valid words in the puzzle.
     * @param validWords List of all valid words that can be found
     * @param wordsFound List tracking which words have been discovered
     */
    public HintTable(List<String> validWords, List<Boolean> wordsFound) {
        this.validWords = copyList(validWords);
        this.wordsFound = wordsFound;

        sortByLengthDescThenAlphabetically(this.validWords);

        fontLayout = new GlyphLayout();

        initializeFont();
        setupScrollAnimation();
    }

    // ========== Initialization ==========

    private void initializeFont() {
        font = Source.generateFont(DataManager.fontName, fontSize);
    }

    /**
     * Sets up the scroll animation that responds to mouse input on the left side.
     */
    private void setupScrollAnimation() {
        Source.addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                handleScrollInput(delta);
            }
        }));
    }

    // ========== Utility Methods ==========

    /**
     * Creates a defensive copy of a string list.
     */
    private List<String> copyList(List<String> list) {
        return new ArrayList<>(list);
    }

    /**
     * Sorts words by length (longest first), then alphabetically.
     * @param list List to sort in-place
     */
    public static void sortByLengthDescThenAlphabetically(List<String> list) {
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // First compare by length (longest first)
                int lengthCompare = Integer.compare(s2.length(), s1.length());

                // If lengths are equal, compare alphabetically
                if (lengthCompare == 0) {
                    return s1.compareToIgnoreCase(s2);
                }

                return lengthCompare;
            }
        });
    }



    // ========== Scroll Handling ==========

    /**
     * Handles mouse scrolling when hovering over the table area.
     */
    private void handleScrollInput(float delta) {
        boolean isOverTable = Source.getScreenMouseX() > hintX &&
            Source.getScreenMouseX() < Source.getScreenWidth() / 2f &&
            Source.getScreenMouseY() < Source.getScreenHeight() / 2f - scale * 700;

        if (isOverTable && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            scrollMotion = Gdx.input.getDeltaY();
        }

        setScroll(scroll + scrollMotion * delta);
        scrollMotion *= (float) Math.pow(0.1f, delta);
    }

    /**
     * Sets the scroll position with bounds checking.
     * @param newScroll New scroll position
     */
    public void setScroll(float newScroll) {
        int maxScroll = validWords.size() - VISIBLE_ROWS;

        if (newScroll < 0) {
            scroll = 0;
            scrollMotion = 0;
        } else if (newScroll > maxScroll) {
            scroll = maxScroll;
            scrollMotion = 0;
        } else {
            scroll = newScroll;
        }
    }

    // ========== Drawing ==========

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        calculateLayout();
        renderToFrameBuffer(sb);
    }

    /**
     * Calculates screen-relative layout positions and scales.
     */
    private void calculateLayout() {
        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = (float) Math.min(xScale, yScale);
        hintX = Source.getScreenWidth() / 2f - scale * 650;
        hintY = Source.getScreenHeight() / 2f - scale * 1350;
        font.getData().setScale(scale * (64f / fontSize));
    }

    /**
     * Renders the word list to the framebuffer.
     */
    private void renderToFrameBuffer(SpriteBatch sb) {

        font.setColor(Color.WHITE);

        sb.begin();
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            printWord(sb, i);
        }
        sb.end();
    }

    /**
     * Renders a single word at the given row index.
     * Shows as question marks if not found, gold text if found.
     * @param sb SpriteBatch to draw with
     * @param rowIndex Row index (0-9)
     */
    private void printWord(SpriteBatch sb, int rowIndex) {
        int wordIndex = rowIndex + (int) scroll;
        String word;

        // Determine display word and color
        if (!wordsFound.get(wordIndex)) {
            word = HIDDEN_WORD_PLACEHOLDER;
            if(rowIndex == 0){
                font.setColor(1, 1, 1, 1 - scroll % 1);
            } else if(rowIndex == VISIBLE_ROWS - 1){
                font.setColor(1, 1, 1, scroll % 1);
            } else {
                font.setColor(Color.WHITE);
            }
        } else {
            word = validWords.get(wordIndex);
            if(rowIndex == 0){
                font.setColor(1, 0.84313726f, 0, 1 - scroll % 1);
            } else if(rowIndex == VISIBLE_ROWS - 1){
                font.setColor(1, 0.84313726f, 0, scroll % 1);
            } else {
                font.setColor(Color.GOLD);
            }
        }

        float yPos = (rowIndex - scroll % 1) * 75 * scale;

        // Draw each letter
        int actualWordLength = Math.min(validWords.get(wordIndex).length(), 12);
        for (int i = 0; i < actualWordLength; i++) {
            drawLetter(yPos, i * 50 * scale, "" + word.charAt(i), sb);
        }
    }

    /**
     * Draws a single letter at the specified position.
     * @param y Vertical position
     * @param x Horizontal position
     * @param letter Letter to draw
     * @param sb SpriteBatch to draw with
     */
    private void drawLetter(float y, float x, String letter, SpriteBatch sb) {
        fontLayout.setText(font, letter.toUpperCase());

        x += -fontLayout.width / 2 + hintX;
        y += fontLayout.height / 2 + hintY;

        font.draw(sb, letter.toUpperCase(), x, y);
    }

    // ========== Cleanup ==========

    @Override
    public void dispose() {
    }
}
