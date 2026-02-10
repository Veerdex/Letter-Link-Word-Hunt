package com.grantkoupal.letterlink;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.grantkoupal.letterlink.quantum.core.Agent;
import com.grantkoupal.letterlink.quantum.core.Graphic;

public class Settings extends Agent {

    private final BitmapFont font;
    private final GlyphLayout layout;
    private final Texture onSwitchTexture;
    private final Texture offSwitchTexture;
    private final Texture backgroundTexture;
    private final Texture xTexture;
    private final Graphic onSwitch;
    private final Graphic offSwitch;
    private final Graphic background;
    private final Graphic X;
    private boolean mouseDown = false;

    public Settings(){
        onSwitchTexture = new Texture(Source.getAsset("Misc/On Switch.png"));
        onSwitchTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        offSwitchTexture = new Texture(Source.getAsset("Misc/Off Switch.png"));
        offSwitchTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        onSwitch = new Graphic(onSwitchTexture);
        offSwitch = new Graphic(offSwitchTexture);

        backgroundTexture = new Texture(Source.getAsset("Misc/Settings Background.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        background = new Graphic(backgroundTexture);

        xTexture = new Texture(Source.getAsset("Misc/Red X.png"));
        xTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        X = new Graphic(xTexture);

        font = Source.generateFont(DataManager.fontName, 64);
        layout = new GlyphLayout();
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        if(!Board.settingsOpen) return;
        float yScale = Source.getScreenHeight() / 3500f;
        float xScale = Source.getScreenWidth() / 1500f;
        float scale = Math.min(xScale, yScale);
        sb.begin();

        boolean click = false;
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            if(!mouseDown) {
                mouseDown = true;
                click = true;
            }
        } else {
            mouseDown = false;
        }

        float centerX = Source.getScreenWidth() / 2f;
        float centerY = Source.getScreenHeight() / 2f;

        background.setCenter(centerX, centerY);
        background.setScale(2500 * scale / backgroundTexture.getHeight());
        background.draw(sb);

        float alignX = centerX + 300 * scale;
        float musicY = centerY + 200 * scale;
        float soundY = centerY;
        float vibrationY = centerY - 200 * scale;

        font.setColor(Color.WHITE);
        font.getData().setScale(scale * 1.5f);
        layout.setText(font, "Music");
        font.draw(sb, "Music", centerX - 450 * scale, musicY + layout.height / 2f);
        layout.setText(font, "Sound");
        font.draw(sb, "Sound", centerX - 450 * scale, soundY + layout.height / 2f);
        layout.setText(font, "Vibration");
        font.draw(sb, "Vibration", centerX - 450 * scale, vibrationY + layout.height / 2f);

        float xCenterX = 400 * scale + centerX;
        float xCenterY = 975 * scale + centerY;

        X.setScale(scale * 150 / xTexture.getHeight());
        X.setCenter(xCenterX, xCenterY);
        X.draw(sb);

        if(DataManager.music){
            onSwitch.setCenter(alignX, musicY);
            onSwitch.setScale(100 * scale / onSwitchTexture.getHeight());
            onSwitch.draw(sb);
        } else {
            offSwitch.setCenter(alignX, musicY);
            offSwitch.setScale(100 * scale / offSwitchTexture.getHeight());
            offSwitch.draw(sb);
        }

        if(DataManager.sound){
            onSwitch.setCenter(alignX, soundY);
            onSwitch.setScale(100 * scale / onSwitchTexture.getHeight());
            onSwitch.draw(sb);
        } else {
            offSwitch.setCenter(alignX, soundY);
            offSwitch.setScale(100 * scale / offSwitchTexture.getHeight());
            offSwitch.draw(sb);
        }

        if(DataManager.vibration){
            onSwitch.setCenter(alignX, vibrationY);
            onSwitch.setScale(100 * scale / onSwitchTexture.getHeight());
            onSwitch.draw(sb);
        } else {
            offSwitch.setCenter(alignX, vibrationY);
            offSwitch.setScale(100 * scale / offSwitchTexture.getHeight());
            offSwitch.draw(sb);
        }

        float mouseX = Source.getScreenMouseX();
        float mouseY = Source.getScreenMouseY();

        if(click){
            if(Math.abs(mouseX - alignX) < 110 * scale) {
                if (Math.abs(mouseY - musicY) < 50 * scale) {
                    DataManager.music = !DataManager.music;
                } else if (Math.abs(mouseY - soundY) < 50 * scale) {
                    DataManager.sound = !DataManager.sound;
                } else if (Math.abs(mouseY - vibrationY) < 50 * scale) {
                    DataManager.vibration = !DataManager.vibration;
                }
            }
            if(Math.sqrt(Math.pow(mouseX - xCenterX, 2) + Math.pow(mouseY - xCenterY, 2)) < 100 * scale){
                Board.settingsOpen = false;
            }
        }

        sb.end();
    }

    @Override
    public void dispose() {
        onSwitchTexture.dispose();
        offSwitchTexture.dispose();
        backgroundTexture.dispose();
        xTexture.dispose();
    }
}
