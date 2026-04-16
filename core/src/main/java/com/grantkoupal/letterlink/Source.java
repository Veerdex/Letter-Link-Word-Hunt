package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.grantkoupal.letterlink.backend.BackendHandler;
import com.grantkoupal.letterlink.backend.data.SessionData;
import com.grantkoupal.letterlink.quantum.core.Manager;
import com.grantkoupal.letterlink.quantum.core.TimeFrame;
import com.grantkoupal.letterlink.quantum.core.Timer;
import com.grantkoupal.letterlink.quantum.font.FontProvider;

public class Source extends Manager {

    protected static FontProvider fontProvider;

    public Source(FontProvider fontProvider){
        Source.fontProvider = fontProvider;
    }

    @Override
    public void setUp() {

        saveDataEvery10Seconds();

        BackendHandler.startUp(new BackendHandler.StartupCallback() {
            @Override
            public void onSuccess() {
                loadAssets();

                RankHandler.loadTextures();

                MainMenu mm = new MainMenu();
                loadNewPage(mm);
                //LoadingBoardPage lbp = new LoadingBoardPage();
                //loadNewPage(lbp);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });

        //LoadingBoardPage loadingPage = new LoadingBoardPage();
        //loadNewPage(loadingPage);

        //Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }

    private void saveDataEvery10Seconds(){
        Timer t = new Timer(10, Timer.INDEFINITE, new TimeFrame(){
            @Override
            public void run(long iteration) {
                DataManager.saveData();
            }
        });
        add(t);
    }

    /**
     * Loads game textures and fonts.
     */
    private void loadAssets() {
        ThemeManager.setCurrentTheme(SessionData.theme);
        DataManager.setIcon("Checkmarks.png");
    }

    public static BitmapFont generateFont(String path, int size){
        return fontProvider.getFont(path, size);
    }
}
