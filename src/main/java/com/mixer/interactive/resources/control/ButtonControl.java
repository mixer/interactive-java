package com.mixer.interactive.resources.control;

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
     * The size of text on the button.
     */
    private int textSize;

    /**
     * Color of text on the button.
     */
    private String textColor;

    /**
     * Accent color of the button.
     */
    private String accentColor;

    /**
     * Focus color of the button.
     */
    private String focusColor;

    /**
     * Color of the border of the button.
     */
    private String borderColor;

    /**
     * Button's background color.
     */
    private String backgroundColor;

    /**
     * Button's background image.
     */
    private String backgroundImage;

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
    public ButtonControl addToCooldown(Duration duration) {
        return setCooldown(this.cooldown + duration.toMillis());
    }

    /**
     * Returns the text size of the text for this <code>ButtonControl</code>.
     *
     * @return  The text size of the text for this <code>ButtonControl</code>.
     *
     * @since   3.1.0
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Sets the text size for this <code>ButtonControl</code>.
     *
     * @param   size
     *          The size of this <code>ButtonControl</code>'s text.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public ButtonControl setTextSize(int size) {
        this.textSize = size;
        return this;
    }

    /**
     * Returns the text color of this <code>ButtonControl</code>.
     *
     * @return  The text color of this <code>ButtonControl</code>.
     *
     * @since   3.1.0
     */
    public String getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color for this <code>ButtonControl</code>.
     *
     * @param   color
     *          The color of this <code>ButtonControl</code>'s text.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public ButtonControl setTextColor(String color) {
        this.textColor = color;
        return this;
    }

    /**
     * Returns the accent color of this <code>ButtonControl</code>.
     *
     * @return  The accent color of this <code>ButtonControl</code>.
     *
     * @since   3.1.0
     */
    public String getAccentColor() {
        return accentColor;
    }

    /**
     * Sets the accent color for this <code>ButtonControl</code>.
     *
     * @param   color
     *          The accent color of this <code>ButtonControl</code>.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public ButtonControl setAccentColor(String color) {
        this.accentColor = color;
        return this;
    }

    /**
     * Returns the focus color of this <code>ButtonControl</code>.
     *
     * @return  The focus color of this <code>ButtonControl</code>.
     *
     * @since   3.1.0
     */
    public String getFocusColor() {
        return focusColor;
    }

    /**
     * Sets the focus color for this <code>ButtonControl</code>.
     *
     * @param   color
     *          The focus color of this <code>ButtonControl</code>.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public ButtonControl setFocusColor(String color) {
        this.focusColor = color;
        return this;
    }

    /**
     * Returns the border color of this <code>ButtonControl</code>.
     *
     * @return  The border color of this <code>ButtonControl</code>.
     *
     * @since   3.1.0
     */
    public String getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the border color for this <code>ButtonControl</code>.
     *
     * @param   color
     *          The border color of this <code>ButtonControl</code>.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public ButtonControl setBorderColor(String color) {
        this.borderColor = color;
        return this;
    }

    /**
     * Returns the background color of this <code>ButtonControl</code>.
     *
     * @return  The background color of this <code>ButtonControl</code>.
     *
     * @since   3.1.0
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for this <code>ButtonControl</code>.
     *
     * @param   color
     *          The background color of this <code>ButtonControl</code>.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public ButtonControl setBackgroundColor(String color) {
        this.backgroundColor = color;
        return this;
    }

    /**
     * Returns the background image of this <code>ButtonControl</code>.
     *
     * @return  The background image of this <code>ButtonControl</code>.
     *
     * @since   3.2.0
     */
    public String getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * Sets the background image for this <code>ButtonControl</code>.
     *
     * @param   image
     *          The background image of this <code>ButtonControl</code>.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.2.0
     */
    public ButtonControl setBackgroundImage(String image) {
        this.backgroundImage = image;
        return this;
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
                    this.accentColor = ((ButtonControl) o).accentColor;
                    this.backgroundColor = ((ButtonControl) o).backgroundColor;
                    this.borderColor = ((ButtonControl) o).borderColor;
                    this.focusColor = ((ButtonControl) o).focusColor;
                    this.textColor = ((ButtonControl) o).textColor;
                    this.textSize = ((ButtonControl) o).textSize;
                    this.backgroundImage = ((ButtonControl) o).backgroundImage;
                    return true;
                }
            }
        }
        return false;
    }
}
