package com.mixer.interactive.resources.control;

import com.google.common.base.Objects;
import com.mixer.interactive.resources.InteractiveResource;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

/**
 * A <code>InteractiveControl</code> represents a button control on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ButtonControl extends InteractiveControl<ButtonControl> {

    /**
     * JavaScript keycode which participant’s use to trigger this button via their keyboard
     */
    private Integer keycode;

    /**
     * The text displayed on a button
     */
    private String text;

    /**
     * The cost in sparks involved in pressing a button
     */
    private Integer cost;

    /**
     * Value used for rendering the progress bar on the button. Values should be in the range of [0, 1] where 1
     * represents a completely full bar
     */
    private Float progress;

    /**
     * Cooldown that lasts until the provided UTC unix timestamp
     */
    private Long cooldown;

    /**
     * Initializes a new <code>ButtonControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>ButtonControl</code>
     *
     * @since   1.0.0
     */
    public ButtonControl(String controlID) {
        this(controlID, null, new InteractiveControlPosition[0]);
    }

    /**
     * Initializes a new <code>ButtonControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>ButtonControl</code>
     * @param   sceneID
     *          Unique identifier for the <code>InteractiveScene</code> that contains this control
     * @param   position
     *          An array of initial <code>InteractiveControlPosition</code> for this control
     *
     * @since   1.0.0
     */
    public ButtonControl(String controlID, String sceneID, InteractiveControlPosition... position) {
        super(controlID, sceneID, InteractiveControlType.BUTTON, position);
    }

    /**
     * Returns the keycode for this <code>ButtonControl</code>.
     *
     * @return  The keycode for this <code>ButtonControl</code>
     *
     * @since   1.0.0
     */
    public Integer getKeycode() {
        return keycode;
    }

    /**
     * Sets the keycode for this <code>ButtonControl</code>.
     *
     * @param   keycode
     *          The keycode to be used for this <code>ButtonControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public ButtonControl setKeycode(Integer keycode) {
        this.keycode = keycode;
        return getThis();
    }

    /**
     * Returns the text displayed on the <code>ButtonControl</code>.
     *
     * @return  The text displayed on the <code>ButtonControl</code>
     *
     * @since   1.0.0
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text to be displayed on the <code>ButtonControl</code>.
     *
     * @param   text
     *          The text to be displayed on the <code>ButtonControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public ButtonControl setText(String text) {
        this.text = text;
        return getThis();
    }

    /**
     * Returns the Spark cost for this <code>ButtonControl</code>.
     *
     * @return  The Spark cost for this <code>ButtonControl</code>
     *
     * @since   1.0.0
     */
    public Integer getCost() {
        return cost;
    }

    /**
     * Sets the Spark cost for this <code>ButtonControl</code>.
     *
     * @param   cost
     *          The Spark cost for this <code>ButtonControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public ButtonControl setCost(Integer cost) {
        this.cost = cost;
        return getThis();
    }

    /**
     * Returns the value for the progress bar for this <code>ButtonControl</code>.
     *
     * @return  The value for the progress bar for this <code>ButtonControl</code>
     *
     * @since   1.0.0
     */
    public Float getProgress() {
        return progress;
    }

    /**
     * Sets the value for the progress bar for this <code>ButtonControl</code>.
     *
     * @param   progress
     *          The value for the progress bar for this <code>ButtonControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public ButtonControl setProgress(Float progress) {
        this.progress = progress;
        return getThis();
    }

    /**
     * Returns the UTC unix timestamp (in milliseconds) for when the cooldown for this <code>ButtonControl</code>.
     * expires
     *
     * @return  The UTC unix timestamp (in milliseconds) for when the cooldown for this <code>ButtonControl</code>
     *          expires
     *
     * @since   1.0.0
     */
    public Long getCooldown() {
        return cooldown;
    }

    /**
     * Sets the UTC unix timestamp (in milliseconds) for when the cooldown for this <code>ButtonControl</code> expires.
     *
     * @param   cooldown
     *          The UTC unix timestamp (in milliseconds) for when the cooldown for this <code>ButtonControl</code>
     *          expires
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public ButtonControl setCooldown(Number cooldown) {
        this.cooldown = cooldown.longValue();
        return getThis();
    }

    /**
     * Sets the UTC unix timestamp (in milliseconds) for when the cooldown for this <code>ButtonControl</code> expires.
     *
     * @param   cooldown
     *          The <code>Instant</code> for when the cooldown for this <code>ButtonControl</code>
     *          expires
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public ButtonControl setCooldown(Instant cooldown) {
        this.cooldown = cooldown.toEpochMilli();
        return getThis();
    }

    /**
     * Adds the provided <code>Duration</code> to the existing cooldown for this <code>ButtonControl</code>.
     *
     * @param   duration
     *          The <code>Duration</code> to add to the existing cooldown timestamp
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public ButtonControl addToCooldown(Duration duration) {
        return setCooldown(this.cooldown + duration.toMillis());
    }

    /**
     * {@inheritDoc}
     *
     * @see     InteractiveResource#getThis()
     *
     * @since   1.0.0
     */
    @Override
    protected ButtonControl getThis() {
        return this;
    }

    /**
     * Iterates through a <code>Collection</code> of <code>ButtonControls</code>. If <code>this</code> is found to be
     * in the <code>Collection</code> then <code>this</code> has it's values updated.
     *
     * @param   objects
     *          A <code>Collection</code> of <code>ButtonControls</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @see     InteractiveResource#syncIfEqual(Collection)
     *
     * @since   1.0.0
     */
    @Override
    public ButtonControl syncIfEqual(Collection<? extends ButtonControl> objects) {
        if (objects != null) {
            for (ButtonControl object : objects) {
                if (this.equals(object)) {
                    this.meta = object.meta;
                    this.setDisabled(object.isDisabled());
                    this.keycode = object.keycode;
                    this.text = object.text;
                    this.cost = object.cost;
                    this.progress = object.progress;
                    this.cooldown = object.cooldown;
                }
            }
        }
        return getThis();
    }

    /**
     * Returns a <code>String</code> representation of this <code>ButtonControl</code>.
     *
     * @return  <code>String</code> representation of this <code>ButtonControl</code>
     *
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("controlID", getControlID())
                .add("sceneID", getSceneID())
                .add("kind", getKind())
                .add("disabled", isDisabled())
                .add("keycode", keycode)
                .add("text", text)
                .add("cost", cost)
                .add("progress", progress)
                .add("cooldown", cooldown)
                .add("position", getPositions())
                .add("meta", getMeta())
                .toString();
    }
}
