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

import java.util.List;

/**
 * Displays a scrollable table of words that the player has found.
 * Rendered on the right side of the screen with smooth scrolling and fade effects.
 * Automatically scrolls to show newly added words.
 */
public class GuessTable extends Agent {

    // ========================================
    // CONSTANTS - DISPLAY
    // ========================================

    private static final int VISIBLE_ROWS = 8;
    private static final int FONT_SIZE = 32;
    private static final float FONT_SCALE_RATIO = 64f / FONT_SIZE;

    // ========================================
    // CONSTANTS - LAYOUT
    // ========================================

    private static final float SCREEN_HEIGHT_REFERENCE = 3000f;
    private static final float SCREEN_WIDTH_REFERENCE = 1500f;

    private static final float TABLE_Y_OFFSET = -1350f;
    private static final float TABLE_TOP_THRESHOLD = -700f;

    private static final float ROW_HEIGHT = 75f;
    private static final float LETTER_SPACING = 50f;

    // ========================================
    // CONSTANTS - SCROLLING
    // ========================================

    private static final float SCROLL_FRICTION = 0.1f;
    private static final int MIN_VISIBLE_ROWS_BEFORE_SCROLL = 7;

    // ========================================
    // STATE
    // ========================================

    private BitmapFont font;
    private final GlyphLayout fontLayout;
    private final List<String> listOfWordsFound;

    // Scroll state
    private float scroll = 0;
    private float scrollMotion = 0;
    private int listSize;

    // Layout
    private float scale = 1;
    private float tableX = 0;
    private float tableY = 0;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    /**
     * Creates a new GuessTable to display found words.
     * @param listOfWordsFound Reference to the list of discovered words
     */
    public GuessTable(List<String> listOfWordsFound) {
        this.listOfWordsFound = listOfWordsFound;
        this.listSize = listOfWordsFound.size();
        this.fontLayout = new GlyphLayout();

        initializeFont();
        setupScrollAnimation();
    }

    // ========================================
    // INITIALIZATION
    // ========================================

    private void initializeFont() {
        font = Source.generateFont(DataManager.fontName, FONT_SIZE);
    }

    /**
     * Sets up continuous scroll animation that responds to mouse input.
     */
    private void setupScrollAnimation() {
        Source.addAnimation(new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                if (!Board.menuOpen) {
                    handleScrollInput(delta);
                }
            }
        }));
    }

    // ========================================
    // SCROLL HANDLING
    // ========================================

    /**
     * Handles mouse scrolling when hovering over the table area.
     */
    private void handleScrollInput(float delta) {
        if (isMouseOverTable()) {
            updateScrollMotion();
        }

        applyScrollMotion(delta);
        applyScrollFriction(delta);
    }

    /**
     * Checks if mouse is hovering over the table area.
     */
    private boolean isMouseOverTable() {
        return Source.getScreenMouseX() > tableX &&
            Source.getScreenMouseX() > Source.getScreenWidth() / 2f &&
            Source.getScreenMouseY() < Source.getScreenHeight() / 2f + TABLE_TOP_THRESHOLD * scale;
    }

    /**
     * Updates scroll motion based on mouse drag.
     */
    private void updateScrollMotion() {
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            scrollMotion = Gdx.input.getDeltaY();
        }
    }

    /**
     * Applies scroll motion to current scroll position.
     */
    private void applyScrollMotion(float delta) {
        setScroll(scroll + scrollMotion * delta);
    }

    /**
     * Applies friction to gradually slow down scrolling.
     */
    private void applyScrollFriction(float delta) {
        scrollMotion *= (float) Math.pow(SCROLL_FRICTION, delta);
    }

    /**
     * Sets the scroll position with bounds checking.
     * @param newScroll Desired scroll position
     */
    public void setScroll(float newScroll) {
        int maxScroll = calculateMaxScroll();

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

    /**
     * Calculates the maximum allowed scroll position.
     */
    private int calculateMaxScroll() {
        return Math.max(0, listOfWordsFound.size() - MIN_VISIBLE_ROWS_BEFORE_SCROLL);
    }

    /**
     * Auto-scrolls down when a new word is added to the list.
     */
    private void handleNewWordAdded() {
        if (listSize != listOfWordsFound.size()) {
            setScroll(scroll + 1);
            listSize = listOfWordsFound.size();
        }
    }

    // ========================================
    // MAIN DRAW METHOD
    // ========================================

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        calculateLayout();
        handleNewWordAdded();
        renderWordList(sb);
    }

    // ========================================
    // LAYOUT CALCULATION
    // ========================================

    /**
     * Calculates screen-relative layout positions and scales.
     */
    private void calculateLayout() {
        calculateScreenScale();
        calculateTablePosition();
        updateFontScale();
    }

    private void calculateScreenScale() {
        float yScale = Source.getScreenHeight() / SCREEN_HEIGHT_REFERENCE;
        float xScale = Source.getScreenWidth() / SCREEN_WIDTH_REFERENCE;
        scale = Math.min(xScale, yScale);
    }

    private void calculateTablePosition() {
        tableX = Source.getScreenWidth() / 2f;
        tableY = Source.getScreenHeight() / 2f + TABLE_Y_OFFSET * scale;
    }

    private void updateFontScale() {
        font.getData().setScale(scale * FONT_SCALE_RATIO);
    }

    // ========================================
    // RENDERING
    // ========================================

    /**
     * Renders the word list with scrolling and fade effects.
     */
    private void renderWordList(SpriteBatch sb) {
        font.setColor(Color.WHITE);

        sb.begin();
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            renderWordAtRow(sb, i);
        }
        sb.end();
    }

    /**
     * Renders a single word at the given row index with fade effects.
     * @param sb SpriteBatch to draw with
     * @param rowIndex Row index (0 to VISIBLE_ROWS-1)
     */
    private void renderWordAtRow(SpriteBatch sb, int rowIndex) {
        int wordIndex = calculateWordIndex(rowIndex);

        if (!isValidWordIndex(wordIndex)) {
            return;
        }

        String word = listOfWordsFound.get(wordIndex);
        float yPosition = calculateRowYPosition(rowIndex);

        applyFadeEffect(rowIndex);
        renderWord(sb, word, yPosition);
    }

    /**
     * Calculates which word to display at a given row.
     */
    private int calculateWordIndex(int rowIndex) {
        return rowIndex + (int) scroll;
    }

    /**
     * Checks if word index is within bounds.
     */
    private boolean isValidWordIndex(int wordIndex) {
        return wordIndex >= 0 && wordIndex < listOfWordsFound.size();
    }

    /**
     * Calculates the Y position for a row, accounting for sub-pixel scrolling.
     */
    private float calculateRowYPosition(int rowIndex) {
        return (rowIndex - scroll % 1) * ROW_HEIGHT * scale;
    }

    /**
     * Applies fade-in/fade-out effects to first and last visible rows.
     */
    private void applyFadeEffect(int rowIndex) {
        if (rowIndex == 0) {
            // Fade out top row as it scrolls off screen
            float alpha = 1 - scroll % 1;
            font.setColor(1, 1, 1, alpha);
        } else if (rowIndex == VISIBLE_ROWS - 1) {
            // Fade in bottom row as it scrolls into view
            float alpha = scroll % 1;
            font.setColor(1, 1, 1, alpha);
        } else {
            // Full opacity for middle rows
            font.setColor(Color.WHITE);
        }
    }

    /**
     * Renders a complete word letter by letter.
     */
    private void renderWord(SpriteBatch sb, String word, float yPosition) {
        for (int i = 0; i < word.length(); i++) {
            float xOffset = i * LETTER_SPACING * scale;
            renderLetter(sb, yPosition, xOffset, word.charAt(i));
        }
    }

    /**
     * Renders a single letter at the specified position.
     * @param sb SpriteBatch to draw with
     * @param yPosition Vertical position
     * @param xOffset Horizontal offset from table origin
     * @param letter Letter character to draw
     */
    private void renderLetter(SpriteBatch sb, float yPosition, float xOffset, char letter) {
        String letterStr = String.valueOf(letter).toUpperCase();
        fontLayout.setText(font, letterStr);

        float x = tableX + xOffset - fontLayout.width / 2;
        float y = tableY + yPosition + fontLayout.height / 2;

        font.draw(sb, letterStr, x, y);
    }

    // ========================================
    // CLEANUP
    // ========================================

    @Override
    public void dispose() {
        // Font is managed by Source, no disposal needed
    }
}
