package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * A rounded rectangle shape (squircle) that can be drawn with or without outlines.
 * Combines rectangles and circles to create smooth rounded corners.
 */
public class Squircle extends Agent {

    // ========== Constants ==========
    private static final int CORNER_SEGMENTS = 25;

    // ========== Shape Properties ==========
    private float width = 10;
    private float height = 10;
    private float x = 0;
    private float y = 0;
    private float curve = 5;  // Radius of rounded corners

    // ========== Constructor ==========

    /**
     * Creates a squircle with the specified dimensions and corner curve.
     * @param x Center X position
     * @param y Center Y position
     * @param width Total width of the squircle
     * @param height Total height of the squircle
     * @param curve Radius of the rounded corners
     */
    public Squircle(float x, float y, float width, float height, float curve) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.curve = curve;
    }

    // ========== Static Drawing Methods ==========

    /**
     * Draws a squircle at the specified position with the current ShapeRenderer color.
     * @param sr ShapeRenderer to draw with
     * @param x Center X position
     * @param y Center Y position
     * @param width Total width
     * @param height Total height
     * @param curve Corner radius
     */
    public static void drawSquircle(ShapeRenderer sr, float x, float y, float width, float height, float curve) {
        // Draw two rectangles (horizontal and vertical)
        sr.rect(x - width / 2 + curve, y - height / 2, width - curve * 2, height);
        sr.rect(x - width / 2, y - height / 2 + curve, width, height - curve * 2);

        // Draw four corner circles
        sr.circle(x - width / 2 + curve, y - height / 2 + curve, curve, CORNER_SEGMENTS);  // Bottom-left
        sr.circle(x + width / 2 - curve, y - height / 2 + curve, curve, CORNER_SEGMENTS);  // Bottom-right
        sr.circle(x + width / 2 - curve, y + height / 2 - curve, curve, CORNER_SEGMENTS);  // Top-right
        sr.circle(x - width / 2 + curve, y + height / 2 - curve, curve, CORNER_SEGMENTS);  // Top-left
    }

    /**
     * Draws a squircle with a colored outline.
     * @param sr ShapeRenderer to draw with
     * @param fill Fill color for the interior
     * @param outline Outline color
     * @param thickness Thickness of the outline
     * @param x Center X position
     * @param y Center Y position
     * @param width Total width
     * @param height Total height
     * @param curve Corner radius
     */
    public static void drawSquircleWithOutline(ShapeRenderer sr, Color fill, Color outline, float thickness,
                                               float x, float y, float width, float height, float curve) {
        // Draw outer squircle (outline)
        sr.setColor(outline);
        drawSquircle(sr, x, y, width, height, curve);

        // Draw inner squircle (fill)
        sr.setColor(fill);
        float innerCurve = curve * Math.min(
            (width - thickness * 2) / width,
            (height - thickness * 2) / height
        );
        drawSquircle(sr, x, y, width - thickness * 2, height - thickness * 2, innerCurve);
    }

    // ========== Setters ==========

    public void setCurve(float curve) {
        this.curve = curve;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setCenterX(float x) {
        this.x = x;
    }

    public void setCenterY(float y) {
        this.y = y;
    }

    // ========== Getters ==========

    public float getCurve() {
        return curve;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getCenterX() {
        return x;
    }

    public float getCenterY() {
        return y;
    }

    // ========== Agent Implementation ==========

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        drawSquircle(sr, x, y, width, height, curve);
    }

    @Override
    public void dispose() {
        // No resources to dispose
    }
}
