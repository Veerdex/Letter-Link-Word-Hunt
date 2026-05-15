package com.grantkoupal.letterlink;

public class ThemeManager {
    private static String[] themeOptions = new String[]{
        "Cabin",
        "Volcano"
    };
    protected static String currentTheme = "";

    public static void setCurrentTheme(String newTheme){
        if(isValidTheme(newTheme)) {
            if (!newTheme.equals(currentTheme)){
                currentTheme = newTheme;
                DataManager.update();
                SoundManager.update();
            }
        } else {
            currentTheme = "Cabin";
            DataManager.update();
            SoundManager.update();
        }
    }

    private static boolean isValidTheme(String theme){
        for (String themeOption : themeOptions) {
            if (themeOption.equals(theme)) {
                return true;
            }
        }
        return false;
    }
}
