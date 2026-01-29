package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Painter {
    public static void paintOutline(ShapeRenderer sr, float xPos, float yPos, float rotation, float[] distances) {
        float r = (float) (rotation + Math.atan2(distances[1], distances[0]) + Math.PI / 2);
        float distance = (float) Math.sqrt(Math.pow(distances[1], 2) + Math.pow(distances[0], 2));
        float startX = (float) (xPos + Math.sin(r) * distance);
        float startY = (float) (yPos - Math.cos(r) * distance);
        float previousX = startX;
        float previousY = startY;
        float currentX = 0f;
        float currentY = 0f;
        for (int i = 2; i < distances.length; i += 2) {
            r = (float) (rotation + Math.atan2(distances[i + 1], distances[i]) + Math.PI / 2);
            distance = (float) Math.sqrt(Math.pow(distances[i + 1], 2) + Math.pow(distances[i], 2));
            currentX = (float) (xPos + Math.sin(r) * distance);
            currentY = (float) (yPos - Math.cos(r) * distance);
            sr.line(previousX, previousY, currentX, currentY);
            previousX = currentX;
            previousY = currentY;
        }
        sr.line(startX, startY, currentX, currentY);
    }

    public static void paintOutline(ShapeRenderer sr, float xPos, float yPos, float rotation, float[] rotations,
            float[] distances) {
        float r = rotation + rotations[0];
        float distance = distances[0];
        float startX = (float) (xPos + Math.sin(r) * distance);
        float startY = (float) (yPos - Math.cos(r) * distance);
        float previousX = startX;
        float previousY = startY;
        float currentX = 0f;
        float currentY = 0f;
        for (int i = 1; i < rotations.length; i++) {
            r = rotation + rotations[i];
            distance = distances[i];
            currentX = (float) (xPos + Math.sin(r) * distance);
            currentY = (float) (yPos - Math.cos(r) * distance);
            sr.line(previousX, previousY, currentX, currentY);
            previousX = currentX;
            previousY = currentY;
        }
        sr.line(startX, startY, currentX, currentY);
    }

    public static void paintSolid(ShapeRenderer sr, float xPos, float yPos, float rotation, float[] rotations,
            float[] distances, float scale) {
        // precompute vertices
        float[] xs = new float[rotations.length];
        float[] ys = new float[rotations.length];

        for (int i = 0; i < rotations.length; i++) {
            float r = rotation + rotations[i];
            float d = distances[i] * scale;
            xs[i] = (float) (xPos + Math.sin(r) * d);
            ys[i] = (float) (yPos - Math.cos(r) * d);
        }

        // draw filled triangles (fan from center)
        for (int i = 0; i < rotations.length - 1; i++) {
            sr.triangle(xPos, yPos, xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }

        // close last edge
        sr.triangle(xPos, yPos, xs[rotations.length - 1], ys[rotations.length - 1], xs[0], ys[0]);
    }
}
