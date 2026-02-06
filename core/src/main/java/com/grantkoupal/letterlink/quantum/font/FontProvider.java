package com.grantkoupal.letterlink.quantum.font;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

public interface FontProvider{
    BitmapFont getFont(String fontName, int size);
    void dispose();
}
