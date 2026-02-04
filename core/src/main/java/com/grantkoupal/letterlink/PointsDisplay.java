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
    private float displayPoints = 0;

    public PointsDisplay(Board board, Page p){
        this.board = board;
        font = Source.generateFont(DataManager.fontName, 256);
        font.setColor(Color.BLACK);

        Timer a = new Timer(.01f, Timer.INDEFINITE, new TimeFrame(){
            @Override
            public void run(long iteration) {
                if(board.getTotalPoints() > displayPoints){
                    displayPoints += (board.getTotalPoints() - displayPoints + 5) / 100f;
                    displayPoints = Math.min(board.getTotalPoints(), displayPoints);
                }
            }
        });

        p.addTimer(a);
    }

    @Override
    public void dispose() {}

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        float yScale = (Source.getScreenHeight() / 3000f);
        float xScale = (Source.getScreenWidth() / 1500f);
        scale = (float)Math.min(xScale, yScale);
        displayX = Source.getScreenWidth() / 2f;
        displayY = Source.getScreenHeight() / 2f + scale * 1050;
        font.getData().setScale(Math.min(scale / (("" + (int)displayPoints).length() + 1) * 10, 1.1f * scale));
        layout.setText(font, ("" + (int)displayPoints));
        sr.begin(ShapeRenderer.ShapeType.Filled);
        Squircle.drawSquircleWithOutline(sr, Color.WHITE, Color.BLACK, 20 * scale, displayX, displayY, layout.width + 100 * scale, 300 * scale, 75 * scale);
        sr.end();
        sb.begin();
        font.setColor(Color.BLACK);
        font.draw(sb, ("" + (int)displayPoints), displayX - layout.width / 2f, displayY + layout.height / 2f);
        font.setColor(Color.WHITE);
        sb.end();
    }
}
