package com.grantkoupal.letterlink.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ObjectMap;

// In your HTML module
public class PreGeneratedFontProvider implements com.grantkoupal.letterlink.quantum.FontProvider {
    private ObjectMap<String, BitmapFont> fonts;

    public PreGeneratedFontProvider() {
        fonts = new ObjectMap<>();
    }

    @Override
    public BitmapFont getFont(String fontName, int size) {
        String key = fontName + "_" + size;

        if (fonts.containsKey(key)) {
            return fonts.get(key);
        }

        // Load pre-generated bitmap font
        BitmapFont font = new BitmapFont(
            Gdx.files.internal("Fonts/PreGenerated/" + fontName + "/" + fontName + "_" + size + ".fnt"));
        fonts.put(key, font);

        return font;
    }

    @Override
    public void dispose() {
        for (BitmapFont font : fonts.values()) {
            font.dispose();
        }
    }
}
