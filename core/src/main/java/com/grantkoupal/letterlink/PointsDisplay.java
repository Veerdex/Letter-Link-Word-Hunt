package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.*;

public class PointsDisplay extends Agent {

    private Board board;
    private BitmapFont font;
    private GlyphLayout layout = new GlyphLayout();

    private float displayX = 0;
    private float displayY = 0;
    private float scale = 1;
    private int displayPoints = 0;
    private long lastPointIncrease = 0;
    private long difference = 0;

    public PointsDisplay(Board board, Page p){
        this.board = board;
        font = Source.generateFont(DataManager.fontName, 256);
        font.setColor(Color.BLACK);

        Animation a = new Animation(System.nanoTime(), Animation.INDEFINITE, new Action(){
            @Override
            public void run(float delta) {
                if(System.nanoTime() > lastPointIncrease + difference && board.getTotalPoints() > displayPoints){
                    lastPointIncrease = System.nanoTime();
                    difference = 1000000000L / (board.getTotalPoints() - displayPoints + 5);
                    displayPoints += 2;
                }
            }
        });

        p.addAnimation(a);
    }

    @Override
    public void dispose() {}

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        float yScale = (Source.getScreenHeight() / 3000f);
        float xScale = (Source.getScreenWidth() / 1500f);
        scale = (float)Math.min(xScale, yScale);
        displayX = Source.getScreenWidth() / 2f;
        displayY = Source.getScreenHeight() / 2f + scale * 1200;
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.WHITE);
        sr.rect(displayX - 500 * scale, displayY - 150 * scale, 1000 * scale, 300 * scale);
        sr.end();
        font.getData().setScale(Math.min(scale / (("" + displayPoints).length() + 1) * 10, 1.1f * scale));
        layout.setText(font, ("" + displayPoints));
        sb.begin();
        font.setColor(Color.BLACK);
        font.draw(sb, ("" + displayPoints), displayX - layout.width / 2f, displayY + layout.height / 2f);
        font.setColor(Color.WHITE);
        sb.end();
    }
}
