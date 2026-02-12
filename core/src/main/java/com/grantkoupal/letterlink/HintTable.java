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
 * Sorted by length (longest first), then alphabetically.
 * Rendered on the left side of the screen with smooth scrolling.
 */
public class HintTable extends Agent {

    // ========================================
    // CONSTANTS - DISPLAY
    // ========================================

    private static final int VISIBLE_ROWS = 8;
    private static final int FONT_SIZE = 32;
    private static final float FONT_SCALE_RATIO = 64f / FONT_SIZE;
    private static final int MAX_LETTERS_PER_WORD = 12;

    // Placeholder for undiscovered words
    private static final String HIDDEN_WORD_PLACEHOLDER = "??????????????????????????????";

    // ========================================
    // CONSTANTS - COLORS
    // ========================================

    private static final Color COLOR_HIDDEN = Color.WHITE;
    private static final Color COLOR_FOUND = Color.GOLD;
    private static final float GOLD_R = 1f;
    private static final float GOLD_G = 0.84313726f;
    private static final float GOLD_B = 0f;

    // ========================================
    // CONSTANTS - LAYOUT
    // ========================================

    private static final float SCREEN_HEIGHT_REFERENCE = 3000f;
    private static final float SCREEN_WIDTH_REFERENCE = 1500f;

    private static final float TABLE_X_OFFSET = -650f;
    private static final float TABLE_Y_OFFSET = -1350f;
    private static final float TABLE_TOP_THRESHOLD = -700f;

    private static final float ROW_HEIGHT = 75f;
    private static final float LETTER_SPACING = 50f;

    // ========================================
    // CONSTANTS - SCROLLING
    // ========================================

    private static final float SCROLL_FRICTION = 0.1f;

    // ========================================
    // STATE
    // ========================================

    private BitmapFont font;
    private final GlyphLayout fontLayout;
    private final List<String> validWords;
    private final List<Boolean> wordsFound;

    // Scroll state
    private float scroll = 0;
    private float scrollMotion = 0;

    // Layout
    private float scale = 1;
    private float tableX = 0;
    private float tableY = 0;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    /**
     * Creates a new HintTable showing all valid words in the puzzle.
     * @param validWords List of all valid words that can be found
     * @param wordsFound List tracking which words have been discovered
     */
    public HintTable(List<String> validWords, List<Boolean> wordsFound) {
        this.validWords = createSortedCopy(validWords);
        this.wordsFound = wordsFound;
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
     * Creates a sorted copy of the word list.
     * Sorted by length (longest first), then alphabetically.
     */
    private List<String> createSortedCopy(List<String> words) {
        List<String> copy = new ArrayList<String>(words);
        sortByLengthDescThenAlphabetically(copy);
        return copy;
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
     * Checks if mouse is hovering over the table area (left side of screen).
     */
    private boolean isMouseOverTable() {
        return Source.getScreenMouseX() > tableX &&
            Source.getScreenMouseX() < Source.getScreenWidth() / 2f &&
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
        return Math.max(0, validWords.size() - VISIBLE_ROWS);
    }

    // ========================================
    // MAIN DRAW METHOD
    // ========================================

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        calculateLayout();
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
        tableX = Source.getScreenWidth() / 2f + TABLE_X_OFFSET * scale;
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
        font.setColor(COLOR_HIDDEN);

        sb.begin();
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            renderWordAtRow(sb, i);
        }
        sb.end();
    }

    /**
     * Renders a single word at the given row index.
     * Shows as question marks if not found, gold text if found.
     * @param sb SpriteBatch to draw with
     * @param rowIndex Row index (0 to VISIBLE_ROWS-1)
     */
    private void renderWordAtRow(SpriteBatch sb, int rowIndex) {
        int wordIndex = calculateWordIndex(rowIndex);

        if (!isValidWordIndex(wordIndex)) {
            return;
        }

        boolean isFound = wordsFound.get(wordIndex);
        String displayWord = getDisplayWord(wordIndex, isFound);
        float yPosition = calculateRowYPosition(rowIndex);

        applyColorForWord(rowIndex, isFound);
        renderWord(sb, displayWord, wordIndex, yPosition);
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
        return wordIndex >= 0 && wordIndex < validWords.size();
    }

    /**
     * Gets the display string for a word (actual word or placeholder).
     */
    private String getDisplayWord(int wordIndex, boolean isFound) {
        return isFound ? validWords.get(wordIndex) : HIDDEN_WORD_PLACEHOLDER;
    }

    /**
     * Calculates the Y position for a row, accounting for sub-pixel scrolling.
     */
    private float calculateRowYPosition(int rowIndex) {
        return (rowIndex - scroll % 1) * ROW_HEIGHT * scale;
    }

    /**
     * Applies color based on whether word is found and row position.
     * Includes fade effects for first and last visible rows.
     */
    private void applyColorForWord(int rowIndex, boolean isFound) {
        float alpha = calculateRowAlpha(rowIndex);

        if (isFound) {
            font.setColor(GOLD_R, GOLD_G, GOLD_B, alpha);
        } else {
            font.setColor(1, 1, 1, alpha);
        }
    }

    /**
     * Calculates alpha value for fade effects on first and last rows.
     */
    private float calculateRowAlpha(int rowIndex) {
        if (rowIndex == 0) {
            // Fade out top row as it scrolls off screen
            return 1 - scroll % 1;
        } else if (rowIndex == VISIBLE_ROWS - 1) {
            // Fade in bottom row as it scrolls into view
            return scroll % 1;
        } else {
            // Full opacity for middle rows
            return 1f;
        }
    }

    /**
     * Renders a complete word letter by letter, up to max length.
     */
    private void renderWord(SpriteBatch sb, String displayWord, int wordIndex, float yPosition) {
        int actualWordLength = Math.min(validWords.get(wordIndex).length(), MAX_LETTERS_PER_WORD);

        for (int i = 0; i < actualWordLength; i++) {
            float xOffset = i * LETTER_SPACING * scale;
            renderLetter(sb, yPosition, xOffset, displayWord.charAt(i));
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
