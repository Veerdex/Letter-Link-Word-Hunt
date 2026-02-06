package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.grantkoupal.letterlink.quantum.core.Action;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.Animation;
import com.grantkoupal.letterlink.quantum.core.Page;

/**
 * A sleek loading animation with expanding/contracting bars in a wave pattern.
 * Modern, minimal design with smooth pulsing motion.
 */
public class LoadingAnimation extends Agent {

    private Animation animation;

    // Position and size
    private float x, y;
    private float barWidth = 8f;
    private float barSpacing = 16f;
    private float maxBarHeight = 100f;
    private float scale = 1;

    // Animation state
    private float time = 0f;
    private static final int BAR_COUNT = 7;

    // Colors - gradient from cyan to purple
    private Color startColor = new Color(0.2f, 0.8f, 1.0f, 1f);
    private Color endColor = new Color(0.8f, 0.2f, 1.0f, 1f);

    public LoadingAnimation(float x, float y, Page p) {
        this.x = x;
        this.y = y;

        // Create the animation
        animation = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action() {
            @Override
            public void run(float delta) {
                time += delta * 2.5f; // Animation speed
            }
        });

        p.addAnimation(animation);
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = Math.min(xScale, yScale) * 3f;

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Fixed: totalWidth should be (BAR_COUNT - 1) and don't multiply startX by scale again
        float totalWidth = (BAR_COUNT - 1) * barSpacing * scale;
        float startX = x - totalWidth / 2f;

        for (int i = 0; i < BAR_COUNT; i++) {
            drawBar(sr, i, startX + i * barSpacing * scale);
        }

        sr.end();
    }

    private void drawBar(ShapeRenderer sr, int index, float barX) {
        // Each bar has a phase offset for the wave effect
        float phase = time + index * 0.3f;

        // Height oscillates with sine wave
        float height = maxBarHeight * (0.3f + 0.7f * MathUtils.sin(phase)) * scale;

        // Gradient color from start to end based on bar index
        float t = index / (float)(BAR_COUNT - 1);
        Color barColor = new Color(
            MathUtils.lerp(startColor.r, endColor.r, t),
            MathUtils.lerp(startColor.g, endColor.g, t),
            MathUtils.lerp(startColor.b, endColor.b, t),
            1f
        );

        // Add pulsing alpha
        float alpha = 0.7f + 0.3f * MathUtils.sin(phase * 1.2f);
        barColor.a = alpha;

        sr.setColor(barColor);

        // Draw rounded bar (rectangle + circles on ends)
        float halfWidth = barWidth / 2f * scale;
        float halfHeight = height / 2f;

        // Main rectangle
        sr.rect(barX - halfWidth, y - halfHeight, barWidth * scale, height);

        // Top circle
        sr.circle(barX, y + halfHeight, halfWidth, 12);

        // Bottom circle
        sr.circle(barX, y - halfHeight, halfWidth, 12);

        // Glow effect
        sr.setColor(barColor.r, barColor.g, barColor.b, alpha * 0.2f);
        sr.circle(barX, y + halfHeight, barWidth * scale, 12);
        sr.circle(barX, y - halfHeight, barWidth * scale, 12);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(float height) {
        this.maxBarHeight = height;
    }

    public void setColors(Color start, Color end) {
        this.startColor = start;
        this.endColor = end;
    }

    public void dispose() {
    }
}
