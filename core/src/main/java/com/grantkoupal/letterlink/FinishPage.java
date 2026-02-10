package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.Graphic;
import com.grantkoupal.letterlink.quantum.core.Page;

public class FinishPage extends Page {

    private final BoardResult results;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final Texture statBackgroundTexture;
    private final Texture backgroundTexture;
    private final Graphic statBackground;
    private final float HEIGHT = 900;
    private float scale = 1;
    private String time;
    private String score;
    private String boardValue;

    public FinishPage(BoardResult results){
        this.results = results;

        time = convertNumToTime(results.timeSeconds);
        score = formatWithCommas(results.score);
        boardValue = formatWithCommas((int)results.boardValue);

        backgroundTexture = DataManager.backgroundTexture;
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        statBackgroundTexture = new Texture(Source.getAsset("Misc/Stat Background.png"));
        statBackgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        statBackground = new Graphic(statBackgroundTexture);

        font = Source.generateFont(DataManager.fontName, 128);
        layout = new GlyphLayout();

        DataDisplay dd = new DataDisplay();
        add(dd);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void restart() {

    }

    @Override
    public void dispose() {
        statBackgroundTexture.dispose();
    }

    private String formatWithCommas(int number){
        return String.format("%,d", number);
    }

    private String convertNumToTime(long seconds){
        long minutes = seconds / 60;
        seconds -= minutes * 60;
        return add0("" + minutes) + ":" + add0("" + seconds);
    }

    private String add0(String s){
        if(s.length() == 1){
            return "0" + s;
        }
        return s;
    }

    class DataDisplay extends Agent {

        @Override
        public void draw(ShapeRenderer sr, SpriteBatch sb) {
            float yScale = Source.getScreenHeight() / 3000f;
            float xScale = Source.getScreenWidth() / 1500f;
            scale = Math.min(xScale, yScale);
            sb.begin();
            float centerX = Source.getScreenWidth() / 2f;
            float centerY = Source.getScreenHeight() / 2f;

            float textY = centerY + HEIGHT * scale;

            sb.setProjectionMatrix(Source.camera.combined);
            sb.setColor(.5f, .5f, .5f, 1);
            sb.draw(backgroundTexture,
                0, 0, 0, 0,
                Source.getScreenWidth(), Source.getScreenHeight(),
                1, 1, 0,
                0, 0,
                Source.getScreenWidth(), Source.getScreenHeight(),
                false, false);

            statBackground.setScale(2000 * scale / statBackgroundTexture.getHeight());
            statBackground.setCenter(centerX, centerY);
            statBackground.draw(sb);

            // Score
            font.getData().setScale(scale * 1.25f);
            layout.setText(font, "Score");
            font.setColor(DataManager.menuColor.r, DataManager.menuColor.g, DataManager.menuColor.b, 1);
            font.draw(sb, "Score", centerX - layout.width / 2, centerY + 925 * scale);
            font.getData().setScale(scale * 2f);
            layout.setText(font, score);
            sb.setShader(Shader.glowShader);
            Shader.glowShader.setUniformf("u_brightness", 1.25f);
            font.draw(sb, score, centerX - layout.width / 2, centerY + 775 * scale);
            font.getData().setScale(scale * .75f);
            layout.setText(font, "" + limitDecimal(results.userRank));
            font.draw(sb, "" + limitDecimal(results.userRank), centerX + 650 * scale - layout.width, centerY + 500 * scale);
            layout.setText(font, boardValue);
            font.draw(sb, boardValue, centerX + 650 * scale - layout.width, centerY + 345 * scale);
            layout.setText(font, "" + results.totalWords);
            font.draw(sb, "" + results.totalWords, centerX + 650 * scale - layout.width, centerY + 190 * scale);
            layout.setText(font, results.longestWord);
            if(layout.width > 550 * scale) {
                font.getData().setScale(550 * scale / layout.width * scale * .75f);
                layout.setText(font, results.longestWord);
            }
            font.draw(sb, results.longestWord, centerX + 650 * scale - layout.width, centerY + 35 * scale);
            font.getData().setScale(scale * .75f);
            layout.setText(font, "" + limitDecimal(results.averageWordLength));
            font.draw(sb, "" + limitDecimal(results.averageWordLength), centerX + 650 * scale - layout.width, centerY - 120 * scale);
            layout.setText(font, "" + limitDecimal(results.wordsPerSecond));
            font.draw(sb, "" + limitDecimal(results.wordsPerSecond), centerX + 650 * scale - layout.width, centerY - 275 * scale);
            layout.setText(font, "" + limitDecimal(results.pointsPerSecond));
            font.draw(sb, "" + limitDecimal(results.pointsPerSecond), centerX + 650 * scale - layout.width, centerY - 430 * scale);
            layout.setText(font, time);
            font.draw(sb, time, centerX + 650 * scale - layout.width, centerY - 585 * scale);
            layout.setText(font, "" + results.hintsUsed);
            font.draw(sb, "" + results.hintsUsed, centerX + 650 * scale - layout.width, centerY - 740 * scale);

            sb.setShader(null);

            font.draw(sb, "Rank", centerX - 650 * scale, centerY + 500 * scale);
            font.draw(sb, "Board Value", centerX - 650 * scale, centerY + 345 * scale);
            font.draw(sb, "Total Words", centerX - 650 * scale, centerY + 190 * scale);
            font.draw(sb, "Longest Word", centerX - 650 * scale, centerY + 35 * scale);
            font.draw(sb, "Avg Word Length", centerX - 650 * scale, centerY - 120 * scale);
            font.draw(sb, "Words/sec", centerX - 650 * scale, centerY - 275 * scale);
            font.draw(sb, "Points/sec", centerX - 650 * scale, centerY - 430 * scale);
            font.draw(sb, "Duration", centerX - 650 * scale, centerY - 585 * scale);
            font.draw(sb, "Hints Used", centerX - 650 * scale, centerY - 740 * scale);

            sb.end();
        }

        public float limitDecimal(float value) {
            return Math.round(value * 10f) / 10f;
        }

        @Override
        public void dispose() {

        }
    }
}
