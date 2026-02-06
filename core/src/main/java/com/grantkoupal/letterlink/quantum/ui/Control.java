package com.grantkoupal.letterlink.quantum.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.grantkoupal.letterlink.Source;import com.grantkoupal.letterlink.quantum.core.Agent;import com.grantkoupal.letterlink.quantum.core.Manager;

public class Control extends Agent {

    public static enum Type {
        Toggle, Slider, Selection
    };

    private ControlEvent onUpdate;
    private Type type;
    private float x;
    private float y;
    private float width;
    private float height;
    private BitmapFont label;
    private BitmapFont valueDisplay;
    private String text;
    private Color backgroundColor = Color.BLACK;
    private Color labelColor = Color.WHITE;
    private Color controlColor = Color.WHITE;
    private GlyphLayout labelGL = new GlyphLayout();
    private GlyphLayout valueDisplayGL = new GlyphLayout();

    // Boolean
    private boolean active = false;

    // Slider
    private boolean displayAsInteger = true;
    private float value = 50;
    private float min = 0;
    private float max = 100;

    // Selection
    private int option = 0;
    private int totalOptions = 0;
    private String[] optionLabels;

    public Control(float x, float y, float width, float height, String text, Type type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.type = type;


        this.label = Source.generateFont("Fonts/Orbitron-VariableFont_wght.ttf", 200);
        label.getData().setScale(Manager.convertToMeters(100 / label.getCapHeight()), Manager.convertToMeters(100 / label.getCapHeight()));
        label.setUseIntegerPositions(false);
        labelGL.setText(label, text);

        valueDisplay = Source.generateFont("Fonts/Orbitron-VariableFont_wght.ttf", 200);
        valueDisplay.getData().setScale(Manager.convertToMeters(100 / valueDisplay.getCapHeight()), Manager.convertToMeters(100 / valueDisplay.getCapHeight()));
        valueDisplay.setUseIntegerPositions(false);
        valueDisplayGL.setText(valueDisplay, Float.toString(value));

        setTouchable(Touchable.enabled);

        switch (type) {
            case Toggle:
                setBounds(x - width / 2, y - height / 2, width, height);
                booleanControl();
                break;
            case Slider:
                setBounds(x - width / 4 - 5f, y - 5f, width / 2 + 10, 10);
                sliderControl();
                break;
            case Selection:
                setBounds(x - width / 2, y - height / 2, width, height);
                optionControl();
                break;
        }
    }

    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(backgroundColor);
        sr.rect(x - width / 2, y - height / 2, width, height);
        switch (type) {
            case Toggle:
                drawBoolean(sr);
                break;
            case Slider:
                drawSlider(sr, sb);
                break;
            case Selection:
                sr.end();
                drawOption(sb);
                break;
        }

        sb.begin();
        label.setColor(labelColor);
        label.draw(sb, text, x - width / 2.5f, y + labelGL.height / 2);
        sb.end();
    }

    private void booleanControl() {
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                active = !active;
                onUpdate();
            }
        });
    }

    private void sliderControl() {
        Control me = this;
        DragListener drag = new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                Vector2 pointerPos =new Vector2(Gdx.input.getX(), Gdx.input.getY());
                Manager.viewport.unproject(pointerPos);
                value = (pointerPos.x - (me.x - width / 4)) / (width / 2) * (max - min) + min;
                if (value < min) {
                    value = min;
                } else if (value > max) {
                    value = max;
                }
                onUpdate();
            }
        };
        drag.setTapSquareSize(0f);
        addListener(drag);
    }

    private void optionControl(){
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                option += 1;
                if(option >= totalOptions){
                    option = 0;
                }
                onUpdate();
            }
        });
    }

    public void setOptions(String[] o){
        optionLabels = o;
        totalOptions = optionLabels.length;
        option = 0;
    }

    private void drawOption(SpriteBatch sb){
        sb.begin();
        label.setColor(controlColor);
        valueDisplay.draw(sb, optionLabels[option], x + width / 8 - valueDisplayGL.width / 2, y + valueDisplayGL.height / 2);
        sb.end();
    }

    private void drawSlider(ShapeRenderer sr, SpriteBatch sb) {
        sr.setColor(controlColor);
        sr.rect(x - width / 4, y - .5f, width / 2, 1f);
        sr.circle(x - width / 4 + (width / 2) * ((value - min) / (max - min)), y, 5f, 10);
        sr.end();

        sb.begin();
        label.setColor(labelColor);
        if (displayAsInteger) {
            valueDisplay.draw(sb, Integer.toString((int) value), x + width / 2 - width / 8,
                    y + valueDisplayGL.height / 2);
        } else {
            String result = String.valueOf(Math.round(value * 100.0) / 100.0) + "\n";
            valueDisplay.draw(sb, result, x + width / 2 - width / 8,
                    y + valueDisplayGL.height / 2);
        }
        sb.end();
    }

    private void drawBoolean(ShapeRenderer sr) {
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(controlColor);
        sr.rect(x - 5f + width / 4, y - 5f, 10, 10);
        if (active) {
            sr.line(x - 5f + width / 4, y - 5f, x + 5f + width / 4, y + 5f);
            sr.line(x - 5f + width / 4, y + 5f, x + 5f + width / 4, y - 5f);
        }
        sr.end();
    }

    @Override
    public void dispose() {
        label.dispose();
        valueDisplay.dispose();
        remove();
    }

    public void displayAsInteger(boolean b) {
        displayAsInteger = b;
    }

    public void setMaxValue(float m) {
        max = m;
    }

    public void setMinValue(float m) {
        min = m;
    }

    public void setValue(float v) {
        value = v;
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
    }

    public void setActive(boolean b) {
        active = b;
    }

    public void setLabelColor(Color c) {
        labelColor = c;
    }

    public void setControlColor(Color c) {
        controlColor = c;
    }

    public void setBackgroundColor(Color c) {
        backgroundColor = c;
    }

    public void setLabelFont(BitmapFont f) {
        label = f;
        label.getData().setScale(10 / label.getCapHeight(), 10 / label.getCapHeight());
        labelGL.setText(label, text);
    }

    public void setValueFont(BitmapFont f) {
        valueDisplay = f;
        valueDisplay.getData().setScale(10 / valueDisplay.getCapHeight(), 10 / valueDisplay.getCapHeight());
        valueDisplayGL.setText(valueDisplay, text);
    }

    private void onUpdate(){
        if(onUpdate != null){
            onUpdate.run(active, value, option);
        }
    }

    public void setOnUpdate(ControlEvent r){
        onUpdate = r;
    }

    public boolean isActive(){
        return active;
    }

    public float getValue(){
        return value;
    }

    public float getOption(){
        return option;
    }

    public void activate(){
        Manager.addToStage(this);
    }

    public void deactivate(){
        remove();
    }

    public void setOption(int o){
        if(o < 0 || o >= totalOptions){
            return;
        }

        option = o;
    }
}
