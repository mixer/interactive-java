package com.mixer.interactive.resources.control;

import com.google.common.hash.Hashing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

/**
 * A <code>InteractiveControlPosition</code> represents the position and size of the asociated control on the specified
 * control canvas area.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveControlPosition implements Comparable<InteractiveControlPosition> {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Default Interactive control canvas size
     */
    private static final InteractiveCanvasSize DEFAULT_CANVAS_SIZE = InteractiveCanvasSize.LARGE;

    /**
     * Default width
     */
    private static final int DEFAULT_WIDTH = 6;

    /**
     * Default height
     */
    private static final int DEFAULT_HEIGHT = 4;

    /**
     * Default X position
     */
    private static final int DEFAULT_X = 0;

    /**
     * Default Y position
     */
    private static final int DEFAULT_Y = 0;

    /**
     * <code>InteractiveCanvasSize</code> for this <code>InteractiveControlPosition</code>
     */
    private final InteractiveCanvasSize size;

    /**
     * Width of the <code>InteractiveControl</code>
     */
    private int width;

    /**
     * Height of the <code>InteractiveControl</code>
     */
    private int height;

    /**
     * X position on the Interactive control canvas
     */
    private int x;

    /**
     * Y position on the Interactive control canvas
     */
    private int y;

    /**
     * Initializes a new <code>InteractiveControlPosition</code> with default values.
     *
     * @param   size
     *          <code>InteractiveCanvasSize</code> for this <code>InteractiveControlPosition</code>
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition(InteractiveCanvasSize size) {
        this(size != null ? size : DEFAULT_CANVAS_SIZE, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_X, DEFAULT_Y);
    }

    /**
     * Initializes a new <code>InteractiveControlPosition</code>.
     *
     * @param   size
     *          Canvas size for this <code>InteractiveControlPosition</code>
     * @param   width
     *          Width of the <code>InteractiveControl</code>
     * @param   height
     *          Height of the <code>InteractiveControl</code>
     * @param   x
     *          X position on the Interactive control canvas
     * @param   y
     *          Y position on the Interactive control canvas
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition(String size, int width, int height, int x, int y) {
        this(InteractiveCanvasSize.from(size), width, height, x, y);
    }

    /**
     * Initializes a new <code>InteractiveControlPosition</code>.
     *
     * @param   size
     *          <code>InteractiveCanvasSize</code> for this <code>InteractiveControlPosition</code>
     * @param   width
     *          Width of the <code>InteractiveControl</code>
     * @param   height
     *          Height of the <code>InteractiveControl</code>
     * @param   x
     *          X position on the Interactive control canvas
     * @param   y
     *          Y position on the Interactive control canvas
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition(InteractiveCanvasSize size, int width, int height, int x, int y) {
        if (size != null) {
            this.size = size;
        }
        else {
            LOG.fatal("Invalid size");
            throw new IllegalArgumentException("Invalid canvas size");
        }

        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the <code>InteractiveCanvasSize</code> for this control position.
     *
     * @return  <code>InteractiveCanvasSize</code> for this control position
     *
     * @since   1.0.0
     */
    public InteractiveCanvasSize getCanvasSize() {
        return size;
    }

    /**
     * Returns the <code>InteractiveControls</code> width for this control position.
     *
     * @return  The <code>InteractiveControls</code> width for this control position
     *
     * @since   1.0.0
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the <code>InteractiveControls</code> width for this control position.
     *
     * @param   width
     *          The <code>InteractiveControls</code> width for this control position
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition setWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * Returns the <code>InteractiveControls</code> height for this control position.
     *
     * @return  The <code>InteractiveControls</code> height for this control position
     *
     * @since   1.0.0
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the <code>InteractiveControls</code> height for this control position.
     *
     * @param   height
     *          The <code>InteractiveControls</code> height for this control position
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * Sets the <code>InteractiveControls</code> height and width for this control position.
     *
     * @param   width
     *          The <code>InteractiveControls</code> height for this control position
     * @param   height
     *          The <code>InteractiveControls</code> height for this control position
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition resize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Returns the <code>InteractiveControls</code> X position for this control position.
     *
     * @return  The <code>InteractiveControls</code> X position for this control position
     *
     * @since   1.0.0
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the <code>InteractiveControls</code> X position for this control position.
     *
     * @param   x
     *          The <code>InteractiveControls</code> X position for this control position
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition setX(int x) {
        this.x = x;
        return this;
    }

    /**
     * Returns the <code>InteractiveControls</code> Y position for this control position.
     *
     * @return  The <code>InteractiveControls</code> Y position for this control position
     *
     * @since   1.0.0
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the <code>InteractiveControls</code> Y position for this control position.
     *
     * @param   y
     *          The <code>InteractiveControls</code> Y position for this control position
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition setY(int y) {
        this.y = y;
        return this;
    }

    /**
     * Sets the <code>InteractiveControls</code> X and Y positions for this control position.
     *
     * @param   x
     *          The <code>InteractiveControls</code> X position for this control position
     * @param   y
     *          The <code>InteractiveControls</code> Y position for this control position
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition moveTo(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int hashCode() {
        return Hashing.md5().newHasher()
                .putString(size.toString(), StandardCharsets.UTF_8)
                .putInt(width)
                .putInt(height)
                .putInt(x)
                .putInt(y)
                .hash()
                .asInt();
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int compareTo(InteractiveControlPosition o) {
        return (o != null) ? size.compareTo(o.size) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof InteractiveControlPosition && this.compareTo((InteractiveControlPosition) o) == 0;
    }
}