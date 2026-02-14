package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import java.util.Scanner;

/**
 * Manages and caches game textures and font assets.
 * Provides centralized access to commonly used visual resources.
 */
public class DataManager {

    // ========== Asset Paths ==========
    private static final String THEME_PATH = "Themes/";
    private static final String TILE_PATH = "/Tile.png";
    private static final String BOARD_PATH = "/Board.png";
    private static final String BACKGROUND_PATH = "/GameBackground.png";
    private static final String BOTTOM_TEXT_PATH = "/TextBackground.png";
    private static final String MORE_PATH = "/More.png";
    private static final String SETTINGS_PATH = "/Settings.png";
    private static final String FINISH_PATH = "/Finish.png";
    private static final String RESUME_PATH = "/Resume.png";
    private static final String BACK_PATH = "/Back.png";
    private static final String ICON_PATH = "Icons/";
    private static final String DATA_PATH = "/data.txt";

    // ========== Textures ==========
    protected static Texture tileTexture = null;
    protected static float tileScale = 1;
    protected static Texture boardTexture = null;
    protected static float boardScale = 1;
    protected static Texture backgroundTexture = null;
    protected static Texture bottomTextTexture = null;
    protected static float bottomTextScale = 1;
    protected static Texture iconTexture = null;
    protected static Texture backButtonTexture = null;
    protected static Texture finishButtonTexture = null;
    protected static Texture moreButtonTexture = null;
    protected static Texture resumeButtonTexture = null;
    protected static Texture settingsButtonTexture = null;


    protected static Color chainColor = new Color(1, 0, 0, .25f);
    protected static Color tileTextColor = new Color(1, 1, 1, 1f);
    protected static boolean tileTextOutline = true;
    protected static Color menuColor = new Color(1, 1, 1, .25f);

    // ========== Font ==========
    protected static String fontName;

    // ========== User Info ==========
    protected static String userName = null;
    protected static int rank = 1000;

    protected static boolean vibration = false;
    protected static boolean sound = false;
    protected static boolean music = false;

    // ========== Asset Loading ==========

    public static void update(){
        readValues();
        updateBackgroundTexture();
        updateBoardTexture();
        updateBottomTextTexture();
        updateTileTexture();
    }

    private static void readValues(){
        Scanner scan = new Scanner(Source.getAsset(THEME_PATH + ThemeManager.currentTheme + DATA_PATH).readString());
        updateFontName(scan.nextLine());
        updateScalers(scan.nextLine());
        updateTraceColor(scan.nextLine());
        updateTileLetterColor(scan.nextLine());
        updateMenuColor(scan.nextLine());
        updateButtonTextures();
        scan.close();
    }

    private static void updateButtonTextures(){
        if(settingsButtonTexture != null){
            settingsButtonTexture.dispose();
        }

        if(moreButtonTexture != null){
            moreButtonTexture.dispose();
        }

        if(resumeButtonTexture != null){
            resumeButtonTexture.dispose();
        }

        if(backButtonTexture != null){
            backgroundTexture.dispose();
        }

        if(finishButtonTexture != null){
            finishButtonTexture.dispose();
        }

        settingsButtonTexture = new Texture(Source.getAsset(THEME_PATH + ThemeManager.currentTheme + SETTINGS_PATH));
        settingsButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        moreButtonTexture = new Texture(Source.getAsset(THEME_PATH + ThemeManager.currentTheme + MORE_PATH));
        moreButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        resumeButtonTexture = new Texture(Source.getAsset(THEME_PATH + ThemeManager.currentTheme + RESUME_PATH));
        resumeButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        backButtonTexture = new Texture(Source.getAsset(THEME_PATH + ThemeManager.currentTheme + BACK_PATH));
        backButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        finishButtonTexture = new Texture(Source.getAsset(THEME_PATH + ThemeManager.currentTheme + FINISH_PATH));
        finishButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private static void updateMenuColor(String line){
        Scanner scan = new Scanner(line);
        menuColor = new Color(scan.nextFloat() / 255f, scan.nextFloat() / 255f, scan.nextFloat() / 255f, .75f);
        scan.close();
    }

    private static void updateTraceColor(String line){
        Scanner scan = new Scanner(line);
        chainColor = new Color(scan.nextFloat() / 255f, scan.nextFloat() / 255f, scan.nextFloat() / 255f, 1);
        scan.close();
    }

    private static void updateTileLetterColor(String line){
        Scanner scan = new Scanner(line);
        tileTextColor = new Color(scan.nextFloat() / 255f, scan.nextFloat() / 255f, scan.nextFloat() / 255f, scan.nextFloat() / 255f);
        scan.close();
    }

    private static void updateScalers(String line){
        Scanner scan = new Scanner(line);
        boardScale = scan.nextFloat();
        tileScale = scan.nextFloat();
        bottomTextScale = scan.nextFloat();
        scan.close();
    }

    private static void updateTileTexture(){
        if(tileTexture != null){
            tileTexture.dispose();
        }

        tileTexture = new Texture(Source.getAsset(THEME_PATH + ThemeManager.currentTheme + TILE_PATH), true);
        tileTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
    }

    private static void updateBoardTexture(){
        if(boardTexture != null){
            boardTexture.dispose();
        }

        boardTexture = new Texture(Source.getAsset(THEME_PATH + ThemeManager.currentTheme + BOARD_PATH));
        boardTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private static void updateBackgroundTexture(){
        if(backgroundTexture != null){
            backgroundTexture.dispose();
        }

        backgroundTexture = new Texture(THEME_PATH + ThemeManager.currentTheme + BACKGROUND_PATH);
    }

    private static void updateBottomTextTexture(){
        if(bottomTextTexture != null){
            bottomTextTexture.dispose();
        }

        bottomTextTexture = new Texture(THEME_PATH + ThemeManager.currentTheme + BOTTOM_TEXT_PATH);
        bottomTextTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private static void updateFontName(String line){
        Scanner scan = new Scanner(line);
        DataManager.fontName = scan.next();
        scan.close();
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
        if(finishButtonTexture != null){
            finishButtonTexture.dispose();
        }
        if(backButtonTexture != null){
            backButtonTexture.dispose();
        }
        if(settingsButtonTexture != null){
            settingsButtonTexture.dispose();
        }
        if(resumeButtonTexture != null){
            resumeButtonTexture.dispose();
        }
        if(moreButtonTexture != null){
            moreButtonTexture.dispose();
        }
    }
}
