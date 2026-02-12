package com.grantkoupal.letterlink;

public class ThemeManager {
    private static String[] themeOptions = new String[]{
        "Cabin",
        "Volcano"
    };
    protected static String currentTheme = "Cabin";

    public static void setCurrentTheme(String newTheme){
        if(isValidTheme(newTheme)){
            currentTheme = newTheme;
            DataManager.update();
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
