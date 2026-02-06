package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.Graphic;

public class MenuDisplay extends Agent {

    private final int fontSize = 64;
    private final int MENU_HEIGHT = 325;
    private float scale = 1;
    private final Texture hintTexture;
    private final Graphic icon;
    private final Graphic hint;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final long startTime;

    public MenuDisplay(){

        hintTexture = new Texture(Source.getAsset("Misc/Light Bulb.png"));
        hintTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        icon = new Graphic(DataManager.iconTexture);
        hint = new Graphic(hintTexture);

        font = Source.generateFont(DataManager.fontName, fontSize);

        float yScale = Source.getScreenHeight() / 3000f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = Math.min(xScale, yScale);

        font.getData().setScale(scale);

        layout = new GlyphLayout(font, DataManager.userName);

        startTime = System.currentTimeMillis();
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        float yScale = Source.getScreenHeight() / 3500f;
        float xScale = Source.getScreenWidth() / 1500f;
        scale = (float) Math.min(xScale, yScale);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.WHITE);
        sr.rect(0, Source.getScreenHeight() - MENU_HEIGHT * scale, Source.getScreenWidth(), MENU_HEIGHT * scale);
        sr.setColor(Color.BLACK);
        sr.rectLine(0, Source.getScreenHeight() - (MENU_HEIGHT + 5) * scale, Source.getScreenWidth(), Source.getScreenHeight() - (MENU_HEIGHT + 5) * scale, 25 * scale);
        sr.end();

        icon.setScale(MENU_HEIGHT * scale / icon.getTexture().getWidth() * .833f);
        icon.setCenter(MENU_HEIGHT * scale / 2, Source.getScreenHeight() - MENU_HEIGHT * scale / 2);

        hint.setScale(MENU_HEIGHT * scale / icon.getTexture().getWidth() * .5f);
        hint.setCenter(Source.getScreenWidth() / 2f + 400 * scale, Source.getScreenHeight() - MENU_HEIGHT * scale / 2);

        font.getData().setScale(scale * (128f / fontSize));
        String clock = convertNumToTime(System.currentTimeMillis() - startTime);
        layout.setText(font, clock);

        sb.begin();
        hint.draw(sb);
        icon.draw(sb);
        font.setColor(Color.BLACK);
        font.draw(sb, clock, Source.getScreenWidth() / 2f - layout.width / 2f, Source.getScreenHeight() - MENU_HEIGHT * scale * .833f + layout.height - 20 * scale);
        sb.end();
    }

    private String convertNumToTime(long millis){
        long minutes = millis / 60000;
        millis -= minutes * 60000;
        return add0("" + minutes) + ":" + add0("" + (millis / 1000));
    }

    private String add0(String s){
        if(s.length() == 1){
            return "0" + s;
        }
        return s;
    }

    private String constrain(String s, GlyphLayout gl){
        String name = DataManager.userName;
        if(gl.width > Source.getScreenWidth() / 2f - 175 * scale){
            int i = s.length();
            while(gl.width > Source.getScreenWidth() / 2f - 250 * scale){
                i--;
                layout.setText(font, name.substring(0, i));
            }
            return s.substring(0, i) + "...";
        } else {
            return s;
        }
    }

    @Override
    public void dispose() {
        hintTexture.dispose();
    }
}
