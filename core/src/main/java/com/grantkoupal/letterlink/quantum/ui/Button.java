package com.grantkoupal.letterlink.quantum.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.Actor;import com.grantkoupal.letterlink.quantum.core.Agent;import com.grantkoupal.letterlink.quantum.core.Manager;


public class Button extends Agent {
    private String text;
    private BitmapFont font = new BitmapFont();
    private Color buttonColor = Color.BLUE;
    private Color labelColor = Color.WHITE;
    private ClickListener onClick;
    private InputListener onContact;
    private float curve = 1;
    private float width;
    private float height;
    private float scaleX = 1;
    private float scaleY = 1;
    private float xPos;
    private float yPos;
    private float fontSize = 10;
    private float baseCapHeight;
    private GlyphLayout gl = new GlyphLayout();

    public Button(String text, float x, float y, float width, float height){
        this.text = text;
        setBounds(x - width / 2, y - height / 2, width, height);
        baseCapHeight = font.getCapHeight();
        font.getData().setScale(fontSize / baseCapHeight);
        font.setUseIntegerPositions(false);
        this.width = width;
        this.height = height;
        xPos = x;
        yPos = y;
        gl.setText(font, text);
    }

    /**
     * Creates a new click listener and removes the previous one(if it isn't null)
     *
     * @param action The action to be performed when the button is clicked
     */
    public void onClick(Runnable action){
        if(action == null) return;
        if(onClick != null){
            this.removeListener(onClick);
        }
        onClick = new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                action.run();
            }
        };
        this.addListener(onClick);
    }

    /**
     * Creates a new mouse enter and removes the previous one(if it isn't null)
     *
     * @param enter The action to be performed when the cursor enters the button
     *
     * @param exit The action to be performed when the cursor exits the button
     */
    public void onContact(Runnable enter, Runnable exit){
        if(enter == null || exit == null) return;
        if(onContact != null){
            this.removeListener(onContact);
        }
        onContact = new InputListener(){
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
                enter.run();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
                exit.run();
            }
        };
        this.addListener(onContact);
    }

    public void setCurve(float c){
        curve = c;
    }

    public float getCurve(){
        return curve;
    }

    /**
     * Changes the color of the button
     * @param c new color for the button
     */
    public void setButtonColor(Color c){
        buttonColor = c;
    }

    /**
     * Changes the color of the label
     * @param c new color for the label
     */
    public void setLabelColor(Color c){
        labelColor = c;
    }

    /**
     * Sets the scale X and scale Y to the same value
     * @param xy
     */
    public void setScaleXY(float xy){
        scaleX = xy;
        scaleY = xy;
        super.setPosition(xPos - width * scaleX / 2, yPos - height * scaleY / 2);
        super.setSize(width * scaleX, height * scaleY);
        font.getData().setScale(fontSize / baseCapHeight * scaleX, fontSize / baseCapHeight * scaleY);
    }

    /**
     * Sets the scale X and scale Y to the same value
     */
    public void setScale(float xy){
        scaleX = xy;
        scaleY = xy;
        super.setPosition(xPos - width * scaleX / 2, yPos - height * scaleY / 2);
        super.setSize(width * scaleX, height * scaleY);
        font.getData().setScale(fontSize / baseCapHeight * scaleX, fontSize / baseCapHeight * scaleY);
    }

    /**
     * Sets the scale X and scale Y separately and simealtaniously
     */
    public void setScale(float x, float y){
        scaleX = x;
        scaleY = y;
        super.setPosition(xPos - width * scaleX / 2, yPos - height * scaleY / 2);
        super.setSize(width * scaleX, height * scaleY);
        font.getData().setScale(fontSize / baseCapHeight * scaleX, fontSize / baseCapHeight * scaleY);
    }

    /**
     * Sets the scale X of the button
     */
    @Override
    public void setScaleX(float x){
        scaleX = x;
        super.setX(xPos - width * scaleX / 2);
        super.setWidth(width * scaleX);
        font.getData().setScale(fontSize / baseCapHeight * scaleX, fontSize / baseCapHeight * scaleY);
    }

    /**
     * Sets the scale Y of the button
     */
    @Override
    public void setScaleY(float y){
        scaleY = y;
        super.setY(yPos - height * scaleY / 2);
        super.setHeight(height * scaleY);
        font.getData().setScale(fontSize / baseCapHeight * scaleX, fontSize / baseCapHeight * scaleY);
    }

    /**
     * Returns the scale X of the button
     */
    @Override
    public float getScaleX(){
        return scaleX;
    }

    /**
     * Returns the scale Y of the button
     */
    @Override
    public float getScaleY(){
        return scaleY;
    }

    /**
     * Sets the label of the button
     * @param label Text for the new label
     */
    public void setLabel(String label){
        text = label;
        gl.setText(font, text);
    }

    /**
     * Sets the new font and font size of the label
     * @param fontPath
     * @param fontSize
     */
    public void setFont(String fontPath, float fontSize){
        this.fontSize = fontSize;
        font.dispose();
        font = new BitmapFont(Gdx.files.internal(fontPath));
        baseCapHeight = font.getCapHeight();
        font.getData().setScale(fontSize / baseCapHeight * scaleX, fontSize / baseCapHeight * scaleY);
        font.setUseIntegerPositions(false);
        gl.setText(font, text);
    }

    public void setFont(BitmapFont font, float fontSize){
        this.fontSize = fontSize;
        this.font.dispose();
        this.font = font;
        baseCapHeight = font.getCapHeight();
        font.getData().setScale(fontSize / baseCapHeight * scaleX, fontSize / baseCapHeight * scaleY);
        font.setUseIntegerPositions(false);
        gl.setText(font, text);
    }

    /**
     * Sets the width of the button
     */
    @Override
    public void setWidth(float x){
        width = x;
        super.setX(xPos - width * scaleX / 2);
        super.setWidth(width);
    }

    /**
     * Sets the height of the button
     */
    @Override
    public void setHeight(float y){
        height = y;
        super.setY(yPos - height * scaleY / 2);
        super.setHeight(height);
    }

    /**
     * Sets the x position of the button
     */
    @Override
    public void setX(float x){
        xPos = x;
        super.setX(xPos - width * scaleX / 2);
    }

    /**
     * Sets the y position of the button
     */
    @Override
    public void setY(float y){
        yPos = y;
        super.setY(yPos - height * scaleY / 2);
    }

    /**
     * Gives the width of the button
     */
    @Override
    public float getWidth(){
        return width;
    }

    /**
     * Gives the height of the button
     */
    @Override
    public float getHeight(){
        return height;
    }

    /**
     * Gives the x position of the button
     */
    @Override
    public float getX(){
        return xPos;
    }

    /**
     * Gives the y position of the button
     */
    @Override
    public float getY(){
        return yPos;
    }

    /**
     *
     * @param batch Draws the button
     * @param parentAlpha Opacity of the parent
     */
    @Override
    public void draw(ShapeRenderer sr, SpriteBatch sb){
        sr.setProjectionMatrix(Manager.camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        sr.setColor(buttonColor);
        float x = super.getX();
        float y = super.getY();
        float curveX = curve * scaleX * 2;
        float curveY = curve * scaleY * 2;
        sr.ellipse(x, y, curveX, curveY); //bottom left
        sr.ellipse(x, y + super.getHeight() - curveY, curveX, curveY); //top left
        sr.ellipse(x + super.getWidth() - curveX, y, curveX, curveY); //bottom right
        sr.ellipse(x + super.getWidth() - curveX, y + super.getHeight() - curveY, curveX, curveY); //top right
        sr.setColor(buttonColor);
        sr.rect(
            x,
            y + curveY / 2f,
            super.getWidth(),
            super.getHeight() - curveY
        );
        sr.rect(
            x + curveX / 2f,
            y,
            super.getWidth() - curveX,
            super.getHeight()
        );
        sr.end();

        sb.begin();
        font.setColor(labelColor);
        font.draw(sb, text, x + super.getWidth() / 2 - gl.width / 2 * scaleX, y + super.getHeight() / 2 + gl.height / 2 * scaleY);
        sb.end();
    }

    @Override
    public void dispose() {
        font.dispose();
        this.clearListeners();
    }

    public void activate(){
        Manager.addToStage(this);
    }

    public void deactivate(){
        remove();
    }
}
