package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.TimeFrame;
import com.grantkoupal.letterlink.quantum.core.Timer;
import com.grantkoupal.letterlink.quantum.paint.Squircle;

/**
 * Displays the player's current score with a smooth counting animation.
 * Shows points in a rounded rectangle (squircle) at the top-center of the screen.
 */
public class PointsDisplay extends Agent {

    // ===== Constants =====
    private static final float UPDATE_INTERVAL = 0.01f;

    private static final float ANIMATION_SPEED_FACTOR = 50f;
    private static final float ANIMATION_ACCELERATION = 5f;

    private static final float MAX_FONT_SCALE = 1.1f;
    private static final float FONT_SCALE_DIVISOR = 10f;

    // ===== Font / Text =====
    private static final int FONT_SIZE = 128;

    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // ===== Data =====
    private float displayPoints = 0f; // animated display value

    // ===== Layout =====
    private float displayX = 0f;
    private float displayY = 0f;
    private float scale = 1f;

    public PointsDisplay() {
        font = createFont();
    }

    // ===== Lifecycle =====

    @Override
    public void frame() {
        getPage().addTimer(createAnimationTimer());
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        updateLayout();
        drawBackground(sr);
        drawPoints(sb);
    }

    @Override
    public void dispose() {
        // No resources to dispose (font managed elsewhere in this project)
    }

    // ===== Setup =====

    private BitmapFont createFont() {
        BitmapFont f = Source.generateFont(com.grantkoupal.letterlink.DataManager.fontName, FONT_SIZE);
        f.setColor(Color.BLACK);
        return f;
    }

    private Timer createAnimationTimer() {
        return new Timer(UPDATE_INTERVAL, Timer.INDEFINITE, new TimeFrame() {
            @Override
            public void run(long iteration) {
                animatePointsTowardTarget();
            }
        });
    }

    // ===== Animation =====

    private void animatePointsTowardTarget() {
        int targetPoints = Board.getTotalPoints();
        if (targetPoints <= displayPoints) return;

        float difference = targetPoints - displayPoints;
        float increment = (difference + ANIMATION_ACCELERATION) / ANIMATION_SPEED_FACTOR;

        displayPoints += increment;
        displayPoints = Math.min(targetPoints, displayPoints);
    }

    // ===== Layout / Draw helpers =====

    private void updateLayout() {
        // Screen scale
        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = Math.min(xScale, yScale);

        // Position
        displayX = Source.getScreenWidth() / 2f;
        displayY = Source.getScreenHeight() / 2f + scale * 1050f;

        // Text + font scale (shrink as digits grow)
        String pointsText = String.valueOf((int) displayPoints);

        float base = (256f / FONT_SIZE);
        float fontScale = scale / (pointsText.length() + 1) * FONT_SCALE_DIVISOR;

        float scaled = fontScale * base;
        float maxScaled = MAX_FONT_SCALE * scale * base;

        font.getData().setScale(Math.min(scaled, maxScaled));
        layout.setText(font, pointsText);
    }

    private void drawBackground(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);

        float boxWidth = layout.width + 100f * scale;
        float boxHeight = 300f * scale;
        float cornerRadius = 75f * scale;
        float outlineThickness = 20f * scale;

        Squircle.drawSquircleWithOutline(
            sr,
            Color.WHITE,   // fill
            Color.BLACK,   // outline
            outlineThickness,
            displayX, displayY,
            boxWidth, boxHeight,
            cornerRadius
        );

        sr.end();
    }

    private void drawPoints(SpriteBatch sb) {
        sb.begin();

        String pointsText = String.valueOf((int) displayPoints);
        float textX = displayX - layout.width / 2f;
        float textY = displayY + layout.height / 2f;

        font.setColor(Color.BLACK);
        font.draw(sb, pointsText, textX, textY);
        font.setColor(Color.WHITE); // reset (kept from your original)

        sb.end();
    }
}
