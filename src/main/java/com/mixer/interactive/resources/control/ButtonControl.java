package com.mixer.interactive.resources.control;

import com.mixer.interactive.resources.InteractiveResource;

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
     * JavaScript keyCode which participant’s use to trigger this button via their keyboard
     */
    private Integer keyCode;

    /**
     * The text displayed on a button
     */
    private String text;

    /**
     * The tooltip text displayed when the participant hovers over the button
     */
    private String tooltip;

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
        this(controlID, null);
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
    public ButtonControl(String controlID, String sceneID, InteractiveControlPosition ... position) {
        super(controlID, sceneID, InteractiveControlType.BUTTON, position);
    }

    /**
     * Returns the keyCode for this <code>ButtonControl</code>.
     *
     * @return  The keyCode for this <code>ButtonControl</code>
     *
     * @since   1.0.0
     */
    public Integer getKeyCode() {
        return keyCode;
    }

    /**
     * Sets the keyCode for this <code>ButtonControl</code>.
     *
     * @param   keyCode
     *          The keyCode to be used for this <code>ButtonControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public ButtonControl setKeyCode(Integer keyCode) {
        this.keyCode = keyCode;
        return this;
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
        return this;
    }

    /**
     * Returns the tooltip text displayed when the participant hovers over the button.
     *
     * @return  The tooltip text displayed when the participant hovers over the button
     *
     * @since   2.0.0
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Sets the tooltip text to be displayed when the participant hovers over the <code>ButtonControl</code>.
     *
     * @param   tooltip
     *          The tooltip text displayed when the participant hovers over the button
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   2.0.0
     */
    public ButtonControl setTooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
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
        return this;
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
        return this;
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
        return this;
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
    public ButtonControl addToCooldown(Number duration) {
        return setCooldown(this.cooldown + duration.longValue());
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
     * Iterates through a <code>Collection</code> of Objects. If <code>this</code> is found to be in the
     * <code>Collection</code> then <code>this</code> has it's values updated.
     *
     * @param   objects
     *          A <code>Collection</code> of Objects
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          provided <code>Collection</code> contains <code>this</code>
     *
     * @see     InteractiveResource#syncIfEqual(Collection)
     *
     * @since   2.0.0
     */
    @Override
    public boolean syncIfEqual(Collection<?> objects) {
        if (objects != null) {
            for (Object o : objects) {
                if (o instanceof ButtonControl && this.equals(o)) {
                    this.meta = ((ButtonControl) o).meta;
                    this.setDisabled(((ButtonControl) o).isDisabled());
                    this.keyCode = ((ButtonControl) o).keyCode;
                    this.text = ((ButtonControl) o).text;
                    this.cost = ((ButtonControl) o).cost;
                    this.progress = ((ButtonControl) o).progress;
                    this.cooldown = ((ButtonControl) o).cooldown;
                    return true;
                }
            }
        }
        return false;
    }
}
