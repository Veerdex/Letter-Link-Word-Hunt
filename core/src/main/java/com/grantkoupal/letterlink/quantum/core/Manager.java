package com.grantkoupal.letterlink.quantum.core;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.grantkoupal.letterlink.quantum.audio.MusicHandler;
import com.grantkoupal.letterlink.quantum.paint.Textures;
import com.grantkoupal.letterlink.quantum.physics.EntityContactListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class Manager extends ApplicationAdapter {

    // ----- Pages -----
    private static Page[] queue = new Page[3];
    private static Page currentPage;

    // ----- Input -----
    public static InputMultiplexer multiplexer = new InputMultiplexer();

    // ----- Viewport -----
    private static Color backgroundColor = Color.WHITE;
    private static Color outlineColor = Color.BLACK;
    public static OrthographicCamera camera;
    public static Viewport viewport;
    protected static Stage stage;
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private static boolean isVSync = true;
    private static int FPS = 100;

    // ----- Physics/Lighting -----
    public static World mainWorld;
    private static RayHandler rayHandler;
    private static PointLight skyLight;
    private static boolean shading = true;
    public static final short IGNORE_CHANNEL = 0x0002;
    public static final short LIGHT_CHANNEL = 0x0004;
    public static final short CONTACT_CHANNEL = 0x0006;
    public static EntityContactListener ECL;
    public static final float PPM = 10f;
    private static OrthographicCamera physicsCamera;
    private static Viewport physicsViewport;

    // ----- System -----
    protected static float totalTime;
    protected static float delta;
    protected static long nanoTime = System.nanoTime();
    protected static boolean printFrameRate = false;
    protected static List<Renderer> renderers = new LinkedList<Renderer>();
    private static Runnable onClose;
    private static float masterVolume;
    private static float musicVolume;
    private static float SFXVolume;
    private static boolean onDesktop = false;

    // ----- Looping -----
    private static List<Animation> animationList = new ArrayList<Animation>();
    private static List<Timer> timerList = new ArrayList<Timer>();
    private static List<Process> resizeList = new ArrayList<Process>();

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
        stage.draw();

        //mainWorld.love

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
                        setScrollAmountX(amountY);
                        setBubbles(false);
                    }});
                }
                return true;
            }
        });
    }

    /**
     * Must be called before any interaction with Box2D
     *
     * @param gravity                (x, y)
     * @param ignoreInactiveBodies Determines whether bodies stay active when
     *                               stationary
     * @param skyLightDirection      Direction the sky light is from, 0 is up (0 ->
     *                               360)
     */
    public static void setUpBox2D(Vector2 gravity, boolean ignoreInactiveBodies, float skyLightDirection) {
        Box2D.init();

        // World
        mainWorld = new World(gravity, ignoreInactiveBodies);
        ECL = new EntityContactListener();
        mainWorld.setContactListener(ECL);

        // Physics camera in METERS
        physicsCamera = new OrthographicCamera();
        physicsViewport = new FitViewport(convertToMeters(WIDTH), convertToMeters(HEIGHT), physicsCamera);
        physicsViewport.apply();
        physicsCamera.position.set(convertToMeters(WIDTH) / 2f, convertToMeters(HEIGHT) / 2f, 0f);
        physicsCamera.update();

        // RayHandler
        rayHandler = new RayHandler(mainWorld);
        rayHandler.setShadows(true);
        rayHandler.setBlurNum(2);
        rayHandler.setAmbientLight(0f);
        rayHandler.setCulling(true);

        // (Optional) one-time set; we’ll also set it each frame in render()
        rayHandler.setCombinedMatrix(physicsCamera);

        // ---- SKY LIGHT IN METERS ----
        float offX = MathUtils.sinDeg(skyLightDirection - 180f) * convertToMeters(2000f);
        float offY = -MathUtils.cosDeg(skyLightDirection - 180f) * convertToMeters(2000f);

        skyLight = new PointLight(rayHandler, 8192);
        skyLight.setColor(Color.WHITE);

        // Distances are meters when using the meters camera
        skyLight.setDistance(convertToMeters(800f));

        // Center in meters + offset
        skyLight.setPosition(convertToMeters(WIDTH) / 2f + offX, convertToMeters(HEIGHT) / 2f + offY);
        skyLight.setSoftnessLength(0f);
    }

    /**
     * Set settings for skylight
     *
     * @param direction    Direction the sky light will be from
     * @param brightness
     * @param ambientLight
     * @param shadows
     */
    public static void setLightSettings(float direction, float brightness, float ambientLight, boolean shadows) {
        /*float locationX = MathUtils.sinDeg(direction) * 1000;
        float locationY = -MathUtils.cosDeg(direction) * 1000;
        skyLight.setColor(new Color(1f, 1f, 1f, brightness));
        skyLight.setPosition(WIDTH / 2 + locationX, HEIGHT / 2 + locationY);
        rayHandler.setShadows(shadows);
        rayHandler.setAmbientLight(ambientLight);*/

        float offX = MathUtils.sinDeg(direction)  * convertToMeters(1000f);
        float offY = -MathUtils.cosDeg(direction) * convertToMeters(1000f);

        skyLight.setColor(new Color(1f, 1f, 1f, brightness));
        skyLight.setPosition(convertToMeters(WIDTH) / 2f + offX, convertToMeters(HEIGHT) / 2f + offY);

        rayHandler.setShadows(shadows);
        rayHandler.setAmbientLight(ambientLight);
    }

    public static void disableSkyLight(){
        skyLight.setActive(false);
    }

    public static void activateSkyLight(){
        skyLight.setActive(true);
    }

    public abstract void setUp();

    // Renders the next frame (called every frame)
    @Override
    public void render() {
        ScreenUtils.clear(outlineColor);

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        // Set the scissor box to match the viewport
        Gdx.gl.glScissor(
            (int)viewport.getScreenX(),
            (int)viewport.getScreenY(),
            (int)viewport.getScreenWidth(),
            (int)viewport.getScreenHeight()
        );

        // Clear only inside the viewport bounds
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        delta = Gdx.graphics.getDeltaTime();

        camera.update();
        stage.act(delta);
        nanoTime = System.nanoTime();

        //May want to change this later to not copy the list
        if(currentPage != null) {
            List<Animation> animationRef = currentPage.animations;
            for (int i = 0; i < animationRef.size(); i++) {
                if (!animationRef.get(i).update(nanoTime, delta)) {
                    animationRef.remove(i);
                    i--;
                }
            }
            List<Timer> timerRef = currentPage.timers;
            for (int i = 0; i < timerRef.size(); i++) {
                if (!timerRef.get(i).update(delta)) {
                    timerRef.remove(i);
                    i--;
                }
            }
        }

        if(mainWorld != null){
            mainWorld.step(delta, 6, 2);  // 6 velocity iterations, 2 position iterations
        }

        for(int i = 0; i < animationList.size(); i++){
            if(!animationList.get(i).update(nanoTime, delta)){
                animationList.remove(i);
                i--;
            }
        }

        for(int i = 0; i < timerList.size(); i++){
            if (!timerList.get(i).update(delta)) {
                timerList.remove(i);
                i--;
            }
        }

        for(int i = 0; i < resizeList.size(); i++){
            if (!resizeList.get(i).run()) {
                resizeList.remove(i);
                i--;
            }
        }

        stage.draw();

        // Render light
        if(mainWorld != null && shading){
            // Keep lights inside the same letterboxed area as your Scene2D viewport
            rayHandler.useCustomViewport(
                viewport.getScreenX(),
                viewport.getScreenY(),
                viewport.getScreenWidth(),
                viewport.getScreenHeight()
            );

            // If you have a player/body, update physicsCamera.position from body.getPosition() here
            physicsCamera.update();

            // Give RayHandler the METERS camera
            rayHandler.setCombinedMatrix(
                physicsCamera.combined,
                physicsCamera.position.x, physicsCamera.position.y,
                physicsCamera.viewportWidth  * physicsCamera.zoom,
                physicsCamera.viewportHeight * physicsCamera.zoom
            );

            rayHandler.updateAndRender();
        }

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

    public static void enableShading(){
        shading = true;
    }

    public static void disableShading(){
        shading = false;
    }

    public static void addToStage(Agent a){
        stage.addActor(a);
    }

    public static void setAmbientLight(Color c){
        rayHandler.setAmbientLight(c);
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
        for (int i = 0; i < queue.length; i++) {
            if (queue[i] == p) {
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
        if (queue[2] != null)
            queue[2].delete();
        queue[2] = queue[1];
        queue[1] = queue[0];
        queue[0] = currentPage;

        currentPage = p;

        addRenderer(p.renderer);
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
        currentPage = queue[0];
        queue[0] = current;
        currentPage.restart();
        addRenderer(currentPage.renderer);
        return currentPage;
    }

    /**
     * Resizes the viewport every frame to make sure display is consistent with
     * window dimensions
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (physicsViewport != null) physicsViewport.update(width, height, true); // meters
        if(currentPage == null) return;
        List<Process> processRef = currentPage.resizes;
        for(int i = 0; i < processRef.size(); i++) {
            if (!processRef.get(i).run()) {
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
        if(rayHandler != null){
            rayHandler.dispose();
        }
        if(mainWorld != null){
            mainWorld.dispose();
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

    public static EntityContactListener getEntityContactListener(){
        if(ECL == null){
            throw new IllegalStateException("Box 2D has not been set up!");
        }
        return ECL;
    }

    public static RayHandler getRayHandler(){
        return rayHandler;
    }

    public static float convertToMeters(float f){
        return f / PPM;
    }

    public static float convertToPixels(float f){
        return f * PPM;
    }

    public static void addTimer(Timer t){
        timerList.add(t);
    }

    public static void removeTimer(Timer t){
        timerList.remove(t);
    }

    public static void addAnimation(Animation a){
        animationList.add(a);
    }

    public static void removeAnimation(Animation a){
        animationList.remove(a);
    }

    public static void addResize(Process p){
        resizeList.add(p);
    }

    public static void removeResize(java.lang.Process p){
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

    public static void setMasterVolume(float f){
        masterVolume = MathUtils.clamp(f, 0, 1);
        MusicHandler.updateVolume();
    }

    public static float getMasterVolume(){
        return masterVolume;
    }

    public static void setMusicVolume(float f){
        musicVolume = MathUtils.clamp(f, 0, 1);
        MusicHandler.updateVolume();
    }

    public static float getMusicVolume(){
        return musicVolume;
    }

    public static void setSFXVolume(float f){
        SFXVolume = MathUtils.clamp(f, 0, 1);
    }

    public static float getSFXVolume(){
        return SFXVolume;
    }

    public static FileHandle getAsset(String path){
        return Gdx.files.internal(path);
    }

    public static FileHandle getFileFromAssets(String path){
        FileHandle file = Gdx.files.internal(path);
        return file;
    }

    public static FileHandle getLocalFile(String path){
        FileHandle file = Gdx.files.local(path);
        return file;
    }

    public static float getViewportWidth(){
        return viewport.getScreenWidth();
    }

    public static float getViewportHeight(){
        return viewport.getScreenHeight();
    }

    public static int getScreenWidth(){
        return Gdx.graphics.getWidth();
    }

    public static int getScreenHeight(){
        return Gdx.graphics.getHeight();
    }

    public static float getFitMouseX(){
        return (Gdx.input.getX() - (getScreenWidth() - getViewportWidth()) / 2) * (WIDTH / getViewportWidth());
    }

    public static float getFitMouseY(){
        return HEIGHT - (Gdx.input.getY() - (getScreenHeight() - getViewportHeight()) / 2) * (HEIGHT / getViewportHeight());
    }

    public static int getScreenMouseX(){
        return Gdx.input.getX();
    }

    public static int getScreenMouseY(){
        return getScreenHeight() - Gdx.input.getY();
    }

    public static void setOnDesktop(boolean b){
        onDesktop = b;
    }

    public static boolean isOnDesktop(){
        return onDesktop;
    }
}
