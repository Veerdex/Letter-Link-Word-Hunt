package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;

/**
 * Base class for all drawable game objects in the quantum framework.
 * Extends libGDX Actor and adds support for custom rendering with ShapeRenderer and SpriteBatch.
 * Agents can be organized by view order and belong to a parent Page.
 */
public abstract class Agent extends Actor implements Disposable {

    // ========== Flags ==========
    protected boolean isEntity = false;

    // ========== Hierarchy ==========
    protected Page parent;
    public Renderer parentRenderer = null;

    // ========== Rendering ==========
    private int viewOrder = 0;

    // ========== Lifecycle Methods ==========

    /**
     * Called once when the Agent is added to a Page.
     * Override to perform initialization that requires the Agent to be staged.
     */
    public void frame() {
        // Default: no-op
    }

    /**
     * Draws the Agent using ShapeRenderer and/or SpriteBatch.
     * @param sr ShapeRenderer for drawing shapes
     * @param sb SpriteBatch for drawing sprites and text
     */
    public abstract void draw(ShapeRenderer sr, SpriteBatch sb);

    /**
     * Disposes of resources used by this Agent.
     * Called when the Agent is removed or the game is shutting down.
     */
    public abstract void dispose();

    /**
     * Removes this Agent from the scene and disposes of its resources.
     */
    public void delete() {
        dispose();
    }

    // ========== Hierarchy Management ==========

    /**
     * Sets the parent Page that owns this Agent.
     * @param page Parent page
     */
    public void setPage(Page page) {
        this.parent = page;
    }

    /**
     * Gets the parent Page that owns this Agent.
     * @return Parent page, or null if not yet assigned
     */
    public Page getPage() {
        return parent;
    }

    // ========== Entity Status ==========

    /**
     * Checks if this Agent is marked as an entity.
     * Entities may have special behavior or handling in the framework.
     * @return true if this is an entity
     */
    public boolean isEntity() {
        return isEntity;
    }

    // ========== View Order Management ==========

    /**
     * Sets the view order (z-index) for rendering.
     * Lower values are drawn first (behind), higher values drawn last (in front).
     * Re-registers with the parent renderer to update rendering order.
     * @param viewOrder New view order value
     */
    public void setViewOrder(int viewOrder) {
        this.viewOrder = viewOrder;

        // Re-register with renderer to update position in render queue
        if (parentRenderer != null) {
            parentRenderer.removeObject(this);
            parentRenderer.addObject(this);
        }
    }

    /**
     * Gets the current view order (z-index) for rendering.
     * @return View order value
     */
    public int getViewOrder() {
        return viewOrder;
    }
}
