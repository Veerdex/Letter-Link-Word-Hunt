package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.grantkoupal.letterlink.quantum.audio.MusicHandler;
import com.grantkoupal.letterlink.quantum.paint.Textures;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class Manager extends ApplicationAdapter {

    // ----- Mouse -----
    private static boolean CLICK = false;
    private static boolean mouseDown = false;

    // ----- Scaling -----
    private static float SCALE = 1;
    private static float ratioX = 1000;
    private static float ratioY = 1000;

    // ----- Additions -----
    private static final LinkedList<ManagerExtension> extensions = new LinkedList<>();

    // ----- Pages -----
    private static final int cachePages = 3;
    private static Page[] queue = new Page[cachePages];
    private static Page currentPage;

    // ----- Input -----
    public static InputMultiplexer multiplexer = new InputMultiplexer();

    // ----- Viewport -----
    protected static Color backgroundColor = Color.WHITE;
    protected static Color outlineColor = Color.BLACK;
    public static OrthographicCamera camera;
    public static Viewport viewport;
    protected static Stage stage;
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private static boolean isVSync = true;
    private static int FPS = 100;

    // ----- System -----
    protected static float totalTime;
    protected static float delta;
    protected static long nanoTime = System.nanoTime();
    protected static boolean printFrameRate = false;
    protected static final List<Renderer> renderers = new LinkedList<>();
    private static Runnable onClose;
    private static boolean onDesktop = false;

    // ----- Looping -----
    private static final List<Animation> animationList = new ArrayList<>();
    private static final List<Timer> timerList = new ArrayList<>();
    private static final List<Resize> resizeList = new ArrayList<>();

    private static final Timer clickDetect = new Timer(250, 1, new TimeFrame(){
        @Override
        public void run(long iteration) {
            mouseDown = false;
        }
    });

    /**
     * Automatically runs at the beginning of every program and creates
     * a OrthographicCamera which removes perspective allowing for
     * special top down games or platformers. The width and height of
     * the viewport is naturally 1280 by 720 but can be easily changed.
     */
    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply();
        camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0);
        camera.update();

        stage = new Stage(viewport); // IMPORTANT: use same viewport as your game
        Gdx.input.setInputProcessor(multiplexer);
        multiplexer.addProcessor(stage);

        setUp();
    }

    public static void sendScrollingEvents(){
        stage.addListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                for (Actor a : stage.getActors()) {
                    a.fire(new InputEvent() {{
                        setType(Type.scrolled);
                        setScrollAmountY(amountY);
                        setScrollAmountX(amountX);
                        setBubbles(false);
                    }});
                }
                return true;
            }
        });
    }

    public abstract void setUp();

    // Renders the next frame (called every frame)
    @Override
    public void render() {
        ScreenUtils.clear(outlineColor);

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        // Set the scissor box to match the viewport
        Gdx.gl.glScissor(
            viewport.getScreenX(),
            viewport.getScreenY(),
            viewport.getScreenWidth(),
            viewport.getScreenHeight()
        );

        // Clear only inside the viewport bounds
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        if(!mouseDown && Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            CLICK = false;
            mouseDown = true;
            clickDetect.restart();
            add(clickDetect);
        } else if(mouseDown && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            CLICK = true;
            mouseDown = false;
        } else {
            CLICK = false;
        }

        SCALE = Math.min(getScreenWidth() / ratioX, getScreenHeight() / ratioY);

        delta = Gdx.graphics.getDeltaTime();
        nanoTime = System.nanoTime();

        camera.update();
        stage.act(delta);

        //May want to change this later to not copy the list
        if(currentPage != null) {
            List<Animation> animationDef = currentPage.animations;
            for(int i = 0; i < animationDef.size(); i++){
                if(!animationDef.get(i).update(nanoTime, delta)){
                    animationDef.remove(i);
                    i--;
                }
            }
            List<Timer> timerDef = currentPage.timers;
            for(int i = 0; i < timerDef.size(); i++){
                if(!timerDef.get(i).update(delta)){
                    timerDef.remove(i);
                    i--;
                }
            }
        }

        extensions.forEach(ManagerExtension::render);

        for(int i = 0; i < animationList.size(); i++){
            if(!animationList.get(i).update(nanoTime, delta)){
                animationList.remove(i);
                i--;
            }
        }

        for(int i = 0; i < timerList.size(); i++){
            if(!timerList.get(i).update(delta)){
                timerList.remove(i);
                i--;
            }
        }

        stage.draw();

        if (printFrameRate) {
            Gdx.app.log("Manager", "FPS: " + Gdx.graphics.getFramesPerSecond());
        }
    }

    /**
     * Enables if the frame rate will be printed
     *
     * @param b Boolean
     */
    public static void printFrameRate(boolean b) {
        printFrameRate = b;
    }

    public static void addRenderer(Renderer r){
        renderers.add(r);
        stage.addActor(r);
    }

    public static void removeRenderer(Renderer r){
        if(renderers.remove(r)){
            r.remove();
        }
    }

    public static void addExtension(ManagerExtension newExtension){
        if(!extensions.contains(newExtension)){
            extensions.add(newExtension);
        }
    }

    public static void removeExtension(ManagerExtension newExtension){
        int index = extensions.indexOf(newExtension);

        if(index != -1){
            extensions.remove(index);
        }
    }

    public static void addToStage(Agent a){
        stage.addActor(a);
    }
    /**
     * Sets the new background color
     *
     * @param c The new color for the background
     */
    public static void setBackgroundColor(Color c) {
        backgroundColor = c;
    }

    /**
     * Sets the new outline color
     *
     * @param c The new color for the outline
     */
    public static void setOutlineColor(Color c) {
        outlineColor = c;
    }

    /**
     * @return The current background color of the window
     */
    public static Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Adds an input to the program
     *
     * @param ip
     */
    public static void addInput(InputProcessor ip) {
        multiplexer.addProcessor(ip);
    }

    public static void removeInput(InputProcessor ip){
        multiplexer.removeProcessor(ip);
    }
    /**
     * Adds the page to the queue and loads the new page
     */
    public static void loadNewPage(Page p) {
        for (Page page : queue) {
            if (page == p) {
                return; // Skips the queueing process below if the page being added is already existing
                // in the queue
            }
        }
        // If a page is already is added it is removed before the next page is added
        // Adds the new page to the queue, moves pages down the queue, and removes the
        // last page
        if(currentPage != null){
            removeRenderer(currentPage.renderer);
            currentPage.removeAgentsFromStage();
        }
        if (queue[cachePages - 1] != null) {
            queue[cachePages - 1].delete();
        }
        for(int i = cachePages - 1; i > 0; i--){
            queue[i] = queue[i - 1];
        }
        queue[0] = currentPage;

        currentPage = p;

        currentPage.frame();
        addRenderer(currentPage.renderer);
    }

    /**
     * Removes the previous page
     *
     * @return Returns the previous page
     */
    public static Page previousPage() {
        // Exception thrown when there is no page to return to
        if (queue[0] == null)
            throw new IllegalStateException("No previous page to return to!");
        Page current = currentPage;
        removeRenderer(currentPage.renderer);
        currentPage.removeAgentsFromStage();
        currentPage = queue[0];
        queue[0] = current;
        currentPage.restart();
        currentPage.frame();
        addRenderer(currentPage.renderer);
        return currentPage;
    }

    /**
     * Resizes the viewport to make sure display is consistent with
     * window dimensions
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        for(ManagerExtension me : extensions){
            me.resize(width, height);
        }
        resizeList.removeIf(r -> !r.resize());
        if(currentPage == null) return;
        List<Resize> processRef = currentPage.resizes;
        for(int i = 0; i < processRef.size(); i++) {
            if (!processRef.get(i).resize()) {
                processRef.remove(i);
                i--;
            }
        }
    }

    /**
     * Ends the program and disposes all elements
     */
    @Override
    public void dispose() {
        if(onClose != null){
            onClose.run();
        }
        stage.dispose();
        Textures.dispose();
        if(currentPage != null) {
            currentPage.delete();
        }
        int size = queue.length;
        for (int i = 0; i < size; i++) {
            Page nextPage = queue[i];
            if (nextPage != null) {
                nextPage.delete();
            }
        }
        for(ManagerExtension me : extensions){
            me.dispose();
        }
        MusicHandler.dispose();
    }

    public static void setVSync(boolean b){
        isVSync = b;
        Gdx.graphics.setVSync(b);
    }

    public static boolean isVSync(){
        return isVSync;
    }

    public static void add(Timer t){
        if(t.isActive) return;
        t.isActive = true;
        t.setUp();
        timerList.add(t);
    }

    public static void remove(Timer t){
        t.isActive = false;
        timerList.remove(t);
    }

    public static void add(Animation a){
        if(a.isActive) return;
        a.isActive = true;
        a.setUp();
        animationList.add(a);
    }

    public static void remove(Animation a){
        a.isActive = false;
        animationList.remove(a);
    }

    public static void add(Resize p){
        resizeList.add(p);
    }

    public static void remove(Resize p){
        resizeList.remove(p);
    }

    public static void setFPS(int f){
        Gdx.graphics.setForegroundFPS(f);
        FPS = f;
    }

    public static int getFPS(){
        return FPS;
    }

    public static void setOnClose(Runnable r){
        onClose = r;
    }

    public static FileHandle getAsset(String path){
        return Gdx.files.internal(path);
    }

    public static FileHandle getLocalFile(String path){
        FileHandle file = Gdx.files.local(path);
        return file;
    }

    public static int getScreenWidth(){
        return Gdx.graphics.getWidth();
    }

    public static int getScreenHeight(){
        return Gdx.graphics.getHeight();
    }

    public static int getMouseX(){
        return Gdx.input.getX();
    }

    public static int getMouseY(){
        return getScreenHeight() - Gdx.input.getY();
    }

    public static void setOnDesktop(boolean b){
        onDesktop = b;
    }

    public static boolean isOnDesktop(){
        return onDesktop;
    }

    public static void setRatioX(float x){
        ratioX = x;
    }

    public static void setRatioY(float y){
        ratioY = y;
    }

    public static void setRatio(float x, float y){
        ratioX = x;
        ratioY = y;
    }

    public static float getRatioX(){
        return ratioX;
    }

    public static float getRatioY(){
        return ratioY;
    }

    public static float getScale(){return SCALE;}

    public static boolean isClick(){return CLICK;}
}
