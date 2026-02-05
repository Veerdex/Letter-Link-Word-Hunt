package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Texture;

/**
 * Manages and caches game textures and font assets.
 * Provides centralized access to commonly used visual resources.
 */
public class DataManager {

    // ========== Asset Paths ==========
    private static final String TILE_PATH = "Boggle Board/Pieces/";
    private static final String BOARD_PATH = "Boggle Board/Boards/";
    private static final String BACKGROUND_PATH = "Backgrounds/";
    private static final String BOTTOM_TEXT_PATH = "Bottom Text Backgrounds/";
    private static final String ICON_PATH = "Icons/";

    // ========== Textures ==========
    protected static Texture tileTexture = null;
    protected static Texture boardTexture = null;
    protected static Texture backgroundTexture = null;
    protected static Texture bottomTextTexture = null;
    protected static Texture iconTexture = null;

    // ========== Font ==========
    protected static String fontName;

    // ========== User Info ==========
    protected static String userName = null;
    protected static int rank = 1000;

    // ========== Asset Loading ==========

    public static void setTileTexture(String tileName){
        if(tileTexture != null){
            tileTexture.dispose();
        }

        tileTexture = new Texture(Source.getAsset(TILE_PATH + tileName), true);
        tileTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
    }

    public static void setBoardTexture(String boardName){
        if(boardTexture != null){
            boardTexture.dispose();
        }

        boardTexture = new Texture(Source.getAsset(BOARD_PATH + boardName));
        boardTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public static void setBackgroundTexture(String backgroundName){
        if(backgroundTexture != null){
            backgroundTexture.dispose();
        }

        backgroundTexture = new Texture(BACKGROUND_PATH + backgroundName);
    }

    public static void setBottomTextTexture(String bottomTextBackgroundName){
        if(bottomTextTexture != null){
            bottomTextTexture.dispose();
        }

        bottomTextTexture = new Texture(BOTTOM_TEXT_PATH + bottomTextBackgroundName);
        bottomTextTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public static void setFontName(String fontName){
        DataManager.fontName = fontName;
    }

    public static void setIcon(String name){
        if(iconTexture != null){
            iconTexture.dispose();
        }

        iconTexture = new Texture(ICON_PATH + name);
        iconTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public static void setUserName(String userName){
        DataManager.userName = userName;
    }

    // ========== Resource Cleanup ==========

    /**
     * Disposes of all loaded textures to free memory.
     * This should be called when the game session ends or when switching themes.
     */
    public static void dispose() {
        if (tileTexture != null) {
            tileTexture.dispose();
        }
        if (boardTexture != null) {
            boardTexture.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (bottomTextTexture != null) {
            bottomTextTexture.dispose();
        }
    }
}
