package com.grantkoupal.letterlink.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ObjectMap;
import com.grantkoupal.letterlink.quantum.FontProvider;

// In your desktop/android launcher or core code
public class FreeTypeFontProvider implements FontProvider {
    private ObjectMap<String, FreeTypeFontGenerator> generators;
    private ObjectMap<String, BitmapFont> fonts;

    public FreeTypeFontProvider() {
        generators = new ObjectMap<>();
        fonts = new ObjectMap<>();
    }

    public BitmapFont getFont(String fontName, int size) {
        String key = fontName + "_" + size;

        if (fonts.containsKey(key)) {
            return fonts.get(key);
        }

        FreeTypeFontGenerator generator = generators.get(fontName);
        if (generator == null) {
            generator = new FreeTypeFontGenerator(Gdx.files.internal("Fonts/TTF/" + fontName + ".ttf"));
            generators.put(fontName, generator);
        }

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.packer = new PixmapPacker(4096, 4096, Pixmap.Format.RGBA8888, 2, false);

        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        BitmapFont font = generator.generateFont(parameter);
        fonts.put(key, font);

        return font;
    }

    public void dispose() {
        for (BitmapFont font : fonts.values()) {
            font.dispose();
        }
        for (FreeTypeFontGenerator gen : generators.values()) {
            gen.dispose();
        }
    }
}
