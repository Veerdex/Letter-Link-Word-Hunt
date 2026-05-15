package com.grantkoupal.letterlink.quantum.physics;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.grantkoupal.letterlink.quantum.core.Manager;
import com.grantkoupal.letterlink.quantum.core.ManagerExtension;

public abstract class Box2DManager extends Manager implements ManagerExtension {

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

    public void render() {

        if(mainWorld != null){
            mainWorld.step(delta, 6, 2);  // 6 velocity iterations, 2 position iterations
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

    public static void enableShading(){
        shading = true;
    }

    public static void disableShading(){
        shading = false;
    }

    public static void setAmbientLight(Color c){
        rayHandler.setAmbientLight(c);
    }

    public void resize(int width, int height) {
        if (physicsViewport != null) physicsViewport.update(width, height, true); // meters
    }

    public void dispose() {
        if(rayHandler != null){
            rayHandler.dispose();
        }
        if(mainWorld != null){
            mainWorld.dispose();
        }
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
}
