package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.Agent;import com.grantkoupal.letterlink.quantum.core.Page;import com.grantkoupal.letterlink.quantum.core.TimeFrame;import com.grantkoupal.letterlink.quantum.core.Timer;import com.grantkoupal.letterlink.quantum.paint.Squircle;

/**
 * Displays the player's current score with a smooth counting animation.
 * Shows points in a rounded rectangle (squircle) at the top-center of the screen.
 */
public class PointsDisplay extends Agent {

    // ========== Constants ==========
    private static final float UPDATE_INTERVAL = 0.01f;
    private static final float ANIMATION_SPEED_FACTOR = 50f;
    private static final float ANIMATION_ACCELERATION = 5f;
    private static final float MAX_FONT_SCALE = 1.1f;
    private static final float FONT_SCALE_DIVISOR = 10f;

    // ========== Graphics ==========
    private BitmapFont font;
    private GlyphLayout layout;
    private final int fontSize = 128;

    // ========== Data ==========
    private Board board;
    private float displayPoints = 0;  // Animated display value

    // ========== Layout ==========
    private float displayX = 0;
    private float displayY = 0;
    private float scale = 1;

    // ========== Constructor ==========

    /**
     * Creates a points display that animates score changes.
     * @param board The game board to track points from
     * @param page The page to add the animation timer to
     */
    public PointsDisplay(Board board, Page page) {
        this.board = board;
        this.layout = new GlyphLayout();

        initializeFont();
        setupAnimationTimer(page);
    }

    // ========== Initialization ==========

    /**
     * Initializes the font with black color for score display.
     */
    private void initializeFont() {
        font = Source.generateFont(com.grantkoupal.letterlink.DataManager.fontName, fontSize);
        font.setColor(Color.BLACK);
    }

    /**
     * Sets up a timer that smoothly animates the displayed points value.
     * Points count up gradually to match the actual score.
     */
    private void setupAnimationTimer(Page page) {
        Timer animationTimer = new Timer(UPDATE_INTERVAL, Timer.INDEFINITE, new TimeFrame() {
            @Override
            public void run(long iteration) {
                animatePointsTowardTarget();
            }
        });

        page.addTimer(animationTimer);
    }

    /**
     * Smoothly increments displayed points toward the actual board points.
     * Uses an accelerating animation for a natural counting effect.
     */
    private void animatePointsTowardTarget() {
        int targetPoints = board.getTotalPoints();

        if (targetPoints > displayPoints) {
            float difference = targetPoints - displayPoints;
            float increment = (difference + ANIMATION_ACCELERATION) / ANIMATION_SPEED_FACTOR;

            displayPoints += increment;
            displayPoints = Math.min(targetPoints, displayPoints);
        }
    }

    // ========== Drawing ==========

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        calculateLayout();
        drawBackground(sr);
        drawPoints(sb);
    }

    /**
     * Calculates screen-relative layout positions and scales.
     */
    private void calculateLayout() {
        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = (float) Math.min(xScale, yScale);

        displayX = Source.getScreenWidth() / 2f;
        displayY = Source.getScreenHeight() / 2f + scale * 1050;

        // Scale font based on number of digits to keep it readable
        String pointsText = String.valueOf((int) displayPoints);
        float fontScale = scale / (pointsText.length() + 1) * FONT_SCALE_DIVISOR;
        font.getData().setScale(Math.min(fontScale * (256f / fontSize), MAX_FONT_SCALE * scale * (256f / fontSize)));

        layout.setText(font, pointsText);
    }

    /**
     * Draws the rounded rectangle background with outline.
     */
    private void drawBackground(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        float boxWidth = layout.width + 100 * scale;
        float boxHeight = 300 * scale;
        float cornerRadius = 75 * scale;
        float outlineThickness = 20 * scale;

        Squircle.drawSquircleWithOutline(
            sr,
            Color.WHITE,           // Fill color
            Color.BLACK,           // Outline color
            outlineThickness,
            displayX, displayY,
            boxWidth, boxHeight,
            cornerRadius
        );

        sr.end();
    }

    /**
     * Draws the points number centered in the box.
     */
    private void drawPoints(SpriteBatch sb) {
        sb.begin();

        String pointsText = String.valueOf((int) displayPoints);
        float textX = displayX - layout.width / 2f;
        float textY = displayY + layout.height / 2f;

        font.setColor(Color.BLACK);
        font.draw(sb, pointsText, textX, textY);
        font.setColor(Color.WHITE);  // Reset color for other uses

        sb.end();
    }

    // ========== Cleanup ==========

    @Override
    public void dispose() {
        // No resources to dispose
    }
}
