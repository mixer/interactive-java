package com.mixer.interactive.resources.control;

import com.mixer.interactive.resources.InteractiveResource;

import java.util.Collection;

/**
 * A <code>InteractiveControl</code> represents a label control on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       3.1.0
 */
public class LabelControl extends InteractiveControl<LabelControl> {

    /**
     * The text displayed on a label
     */
    private String text;

    /**
     * The size of text on the label.
     */
    private int textSize;

    /**
     * Color of text on the label.
     */
    private String textColor;

    /**
     * If the label is underlined
     */
    private Boolean underline;

    /**
     * If the label is bolded
     */
    private Boolean bold;

    /**
     * If the label is italicized
     */
    private Boolean italic;

    /**
     * Initializes a new <code>LabelControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>LabelControl</code>
     *
     * @since   1.0.0
     */
    public LabelControl(String controlID) {
        this(controlID, null);
    }

    /**
     * Initializes a new <code>LabelControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>LabelControl</code>
     * @param   sceneID
     *          Unique identifier for the <code>InteractiveScene</code> that contains this control
     * @param   position
     *          An array of initial <code>InteractiveControlPosition</code> for this control
     *
     * @since   1.0.0
     */
    public LabelControl(String controlID, String sceneID, InteractiveControlPosition ... position) {
        super(controlID, sceneID, InteractiveControlType.LABEL, position);
    }

    /**
     * Returns the text displayed on the <code>LabelControl</code>.
     *
     * @return  The text displayed on the <code>LabelControl</code>
     *
     * @since   1.0.0
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text to be displayed on the <code>LabelControl</code>.
     *
     * @param   text
     *          The text to be displayed on the <code>LabelControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public LabelControl setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Returns the text size of the text for this <code>LabelControl</code>.
     *
     * @return  The text size of the text for this <code>LabelControl</code>.
     *
     * @since   3.1.0
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Sets the text size for this <code>LabelControl</code>.
     *
     * @param   size
     *          The size of this <code>LabelControl</code>'s text.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public LabelControl setTextSize(int size) {
        this.textSize = size;
        return this;
    }

    /**
     * Returns the text color of this <code>LabelControl</code>.
     *
     * @return  The text color of this <code>LabelControl</code>.
     *
     * @since   3.1.0
     */
    public String getTextColor() {
        return textColor;
    }

    /**
     * Sets the text color for this <code>LabelControl</code>.
     *
     * @param   color
     *          The color of this <code>LabelControl</code>'s text.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public LabelControl setTextColor(String color) {
        this.textColor = color;
        return this;
    }

    /**
     * Returns if this <code>LabelControl</code> is underlined.
     *
     * @return  If this <code>LabelControl</code> is underlined.
     *
     * @since   3.1.0
     */
    public Boolean getUnderline() {
        return underline;
    }

    /**
     * Sets if this <code>LabelControl</code> if underlined.
     *
     * @param   underlined
     *          If this <code>LabelControl</code> if underlined.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public LabelControl setUnderline(Boolean underlined) {
        this.underline = underlined;
        return this;
    }

    /**
     * Returns if this <code>LabelControl</code> is bolded.
     *
     * @return  If this <code>LabelControl</code> is bolded.
     *
     * @since   3.1.0
     */
    public Boolean getBold() {
        return bold;
    }

    /**
     * Sets if this <code>LabelControl</code> if bolded.
     *
     * @param   bolded
     *          If this <code>LabelControl</code> if bolded.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public LabelControl setBold(Boolean bolded) {
        this.bold = bolded;
        return this;
    }

    /**
     * Returns if this <code>LabelControl</code> is italicized.
     *
     * @return  If this <code>LabelControl</code> is italicized.
     *
     * @since   3.1.0
     */
    public Boolean getItalic() {
        return italic;
    }

    /**
     * Sets if this <code>LabelControl</code> if italicized.
     *
     * @param   italicized
     *          If this <code>LabelControl</code> if italicized.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public LabelControl setItalic(Boolean italicized) {
        this.italic = italicized;
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
    protected LabelControl getThis() {
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
                if (o instanceof LabelControl && this.equals(o)) {
                    this.meta = ((LabelControl) o).meta;
                    this.setDisabled(((LabelControl) o).isDisabled());
                    this.text = ((LabelControl) o).text;
                    this.underline = ((LabelControl) o).underline;
                    this.bold = ((LabelControl) o).bold;
                    this.italic = ((LabelControl) o).italic;
                    return true;
                }
            }
        }
        return false;
    }
}
