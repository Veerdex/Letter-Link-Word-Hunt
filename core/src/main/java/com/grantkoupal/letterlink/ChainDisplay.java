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
 * Changes color based on word validity: green (valid), yellow (duplicate), red (invalid).
 * Positioned at the top-center of the screen, below the points display.
 */
public class ChainDisplay extends Agent {

    // ========== Constants ==========
    private static final Color RED = new Color(1, 94f / 255, 94f / 255, 1);
    private static final Color GREEN = new Color(94f / 255, 1, 94f / 255, 1);
    private static final Color YELLOW = new Color(1, 1, 94f / 255, 1);

    private static final float MAX_FONT_SCALE = 1.0f;
    private static final float FONT_SCALE_MULTIPLIER = 12f;
    private static final float BOX_PADDING = 100f;
    private static final float BOX_HEIGHT = 150f;
    private static final float CORNER_RADIUS = 75f;
    private static final float OUTLINE_THICKNESS = 20f;

    // ========== Graphics ==========
    private BitmapFont font;
    private GlyphLayout layout;

    // ========== Data ==========
    private Board board;

    // ========== Layout ==========
    private float displayX = 0;
    private float displayY = 0;
    private float scale = 1;

    // ========== Constructor ==========

    /**
     * Creates a chain display that shows the current word being traced.
     * @param board The game board to track letter chains from
     */
    public ChainDisplay(Board board) {
        this.board = board;
        this.layout = new GlyphLayout();

        initializeFont();
    }

    // ========== Initialization ==========

    /**
     * Initializes the font with black color for chain display.
     */
    private void initializeFont() {
        font = Source.generateFont(DataManager.fontName, 128);
        font.setColor(Color.BLACK);
    }

    // ========== Drawing ==========

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        calculateLayout();
        drawBackground(sr);
        drawChainText(sb);
    }

    /**
     * Calculates screen-relative layout positions and scales.
     */
    private void calculateLayout() {
        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = (float) Math.min(xScale, yScale);

        displayX = Source.getScreenWidth() / 2f;
        displayY = Source.getScreenHeight() / 2f + scale * 800;

        // Scale font based on chain length to keep it readable
        String chainText = board.getStringChain();
        float fontScale = scale / (chainText.length() + 1) * FONT_SCALE_MULTIPLIER;
        font.getData().setScale(Math.min(fontScale, scale * MAX_FONT_SCALE));

        layout.setText(font, chainText.toUpperCase());
    }

    /**
     * Draws the rounded rectangle background with color based on word validity.
     * Green = valid new word, Yellow = already found word, Red = invalid word.
     */
    private void drawBackground(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        Color fillColor = determineBackgroundColor();
        float boxWidth = layout.width + BOX_PADDING * scale;
        float boxHeight = BOX_HEIGHT * scale;

        Squircle.drawSquircleWithOutline(
            sr,
            fillColor,
            Color.BLACK,
            OUTLINE_THICKNESS * scale,
            displayX, displayY,
            boxWidth, boxHeight,
            CORNER_RADIUS * scale
        );

        sr.end();
    }

    /**
     * Determines the background color based on the current chain state.
     * @return Color representing word validity
     */
    private Color determineBackgroundColor() {
        String chain = board.getStringChain();

        // Empty chain = white background
        if (chain.length() == 0) {
            return Color.WHITE;
        }

        // Color based on word state
        switch (board.getCurrentState()) {
            case VALID:
                return GREEN;
            case COPY:
                return YELLOW;
            case INVALID:
                return RED;
            default:
                return Color.WHITE;
        }
    }

    /**
     * Draws the letter chain text centered in the box.
     */
    private void drawChainText(SpriteBatch sb) {
        sb.begin();

        String chainText = board.getStringChain().toUpperCase();
        float textX = displayX - layout.width / 2f;
        float textY = displayY + layout.height / 2f;

        font.setColor(Color.BLACK);

        font.draw(sb, chainText, textX, textY);

        sb.end();
    }

    // ========== Cleanup ==========

    @Override
    public void dispose() {
        // No resources to dispose
    }
}
