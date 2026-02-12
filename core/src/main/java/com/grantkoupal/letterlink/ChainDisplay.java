package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.paint.Squircle;

/**
 * Displays the current letter chain being traced by the player.
 * Shows dynamic color feedback based on word validity:
 * - Green: Valid new word
 * - Yellow: Already found (duplicate)
 * - Red: Invalid word
 * - White: Empty chain
 *
 * Positioned at top-center of screen with responsive sizing.
 */
public class ChainDisplay extends Agent {

    // ========================================
    // CONSTANTS - COLORS
    // ========================================

    private static final Color COLOR_INVALID = new Color(1, 94f / 255, 94f / 255, 1);
    private static final Color COLOR_VALID = new Color(94f / 255, 1, 94f / 255, 1);
    private static final Color COLOR_DUPLICATE = new Color(1, 1, 94f / 255, 1);
    private static final Color COLOR_EMPTY = Color.WHITE;
    private static final Color COLOR_TEXT = Color.BLACK;
    private static final Color COLOR_OUTLINE = Color.BLACK;

    // ========================================
    // CONSTANTS - LAYOUT
    // ========================================

    private static final float MAX_FONT_SCALE = 1.0f;
    private static final float FONT_SCALE_MULTIPLIER = 12f;
    private static final float BOX_PADDING = 100f;
    private static final float BOX_HEIGHT = 150f;
    private static final float CORNER_RADIUS = 75f;
    private static final float OUTLINE_THICKNESS = 20f;
    private static final float VERTICAL_OFFSET = 800f;

    // Screen scale references
    private static final float SCREEN_HEIGHT_REFERENCE = 3000f;
    private static final float SCREEN_WIDTH_REFERENCE = 1500f;

    // ========================================
    // STATE
    // ========================================

    private BitmapFont font;
    private final GlyphLayout layout;

    // Computed layout values
    private float displayX = 0;
    private float displayY = 0;
    private float scale = 1;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    /**
     * Creates a chain display that shows the current word being traced.
     */
    public ChainDisplay() {
        this.layout = new GlyphLayout();
        initializeFont();
    }

    // ========================================
    // INITIALIZATION
    // ========================================

    private void initializeFont() {
        font = Source.generateFont(DataManager.fontName, 128);
        font.setColor(COLOR_TEXT);
    }

    // ========================================
    // MAIN DRAW METHOD
    // ========================================

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        String chainText = Board.getStringChain();

        // Early return if chain is empty - nothing to draw
        if (chainText.isEmpty()) {
            return;
        }

        calculateLayout(chainText);
        drawBackground(sr);
        drawChainText(sb, chainText);
    }

    // ========================================
    // LAYOUT CALCULATION
    // ========================================

    /**
     * Calculates screen-relative layout positions and scales.
     * Adjusts font size dynamically based on chain length.
     */
    private void calculateLayout(String chainText) {
        calculateScreenScale();
        calculateDisplayPosition();
        calculateFontScale(chainText);
        updateTextLayout(chainText);
    }

    private void calculateScreenScale() {
        float yScale = Source.getScreenHeight() / SCREEN_HEIGHT_REFERENCE;
        float xScale = Source.getScreenWidth() / SCREEN_WIDTH_REFERENCE;
        scale = Math.min(xScale, yScale);
    }

    private void calculateDisplayPosition() {
        displayX = Source.getScreenWidth() / 2f;
        displayY = Source.getScreenHeight() / 2f + scale * VERTICAL_OFFSET;
    }

    /**
     * Calculates font scale based on chain length to keep text readable.
     * Longer chains get smaller text to fit in the box.
     */
    private void calculateFontScale(String chainText) {
        float fontScale = scale / (chainText.length() + 1) * FONT_SCALE_MULTIPLIER;
        float finalScale = Math.min(fontScale, scale * MAX_FONT_SCALE);
        font.getData().setScale(finalScale);
    }

    private void updateTextLayout(String chainText) {
        layout.setText(font, chainText.toUpperCase());
    }

    // ========================================
    // BACKGROUND RENDERING
    // ========================================

    /**
     * Draws the rounded rectangle background with color based on word validity.
     */
    private void drawBackground(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        Color fillColor = determineBackgroundColor();
        float boxWidth = calculateBoxWidth();
        float boxHeight = calculateBoxHeight();
        float cornerRadius = CORNER_RADIUS * scale;

        Squircle.drawSquircleWithOutline(
            sr,
            fillColor,
            COLOR_OUTLINE,
            OUTLINE_THICKNESS * scale,
            displayX,
            displayY,
            boxWidth,
            boxHeight,
            cornerRadius
        );

        sr.end();
    }

    /**
     * Determines background color based on current chain validation state.
     */
    private Color determineBackgroundColor() {
        Board.LetterState state = Board.getCurrentState();

        switch (state) {
            case VALID:
                return COLOR_VALID;
            case COPY:
                return COLOR_DUPLICATE;
            case INVALID:
                return COLOR_INVALID;
            case UNSELECTED:
            case HINT:
            case HINT_START:
            default:
                return COLOR_EMPTY;
        }
    }

    private float calculateBoxWidth() {
        return layout.width + BOX_PADDING * scale;
    }

    private float calculateBoxHeight() {
        return BOX_HEIGHT * scale;
    }

    // ========================================
    // TEXT RENDERING
    // ========================================

    /**
     * Draws the letter chain text centered in the display box.
     */
    private void drawChainText(SpriteBatch sb, String chainText) {
        sb.begin();

        String displayText = chainText.toUpperCase();
        float textX = calculateTextX();
        float textY = calculateTextY();

        font.setColor(COLOR_TEXT);
        font.draw(sb, displayText, textX, textY);

        sb.end();
    }

    private float calculateTextX() {
        return displayX - layout.width / 2f;
    }

    private float calculateTextY() {
        return displayY + layout.height / 2f;
    }

    // ========================================
    // CLEANUP
    // ========================================

    @Override
    public void dispose() {
        // Font is managed by Source, no disposal needed
    }
}
