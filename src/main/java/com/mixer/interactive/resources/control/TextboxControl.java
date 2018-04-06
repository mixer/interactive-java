package com.mixer.interactive.resources.control;

import com.mixer.interactive.resources.InteractiveResource;

import java.util.Collection;

/**
 * A <code>InteractiveControl</code> represents a label control on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class TextboxControl extends InteractiveControl<TextboxControl> {

    /**
     * Text shown on submit button.
     */
    private String sumbitText;

    /**
     * Placeholder text.
     */
    private String placeholder;

    /**
     * The cost in sparks involved in submitting this text.
     */
    private Integer cost;

    /**
     * Shows if this textbox shows a submit button.
     */
    private boolean hasSubmit;

    /**
     * Shows if this textbox has multiple lines.
     */
    private boolean multiline;

    /**
     * Initializes a new <code>TextboxControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>TextboxControl</code>
     *
     * @since   1.0.0
     */
    public TextboxControl(String controlID) {
        this(controlID, null);
    }

    /**
     * Initializes a new <code>TextboxControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>TextboxControl</code>
     * @param   sceneID
     *          Unique identifier for the <code>InteractiveScene</code> that contains this control
     * @param   position
     *          An array of initial <code>InteractiveControlPosition</code> for this control
     *
     * @since   1.0.0
     */
    public TextboxControl(String controlID, String sceneID, InteractiveControlPosition ... position) {
        super(controlID, sceneID, InteractiveControlType.TEXTBOX, position);
    }

    /**
     * Returns the Spark cost for this <code>TextboxControl</code>.
     *
     * @return  The Spark cost for this <code>TextboxControl</code>
     *
     * @since   1.0.0
     */
    public Integer getCost() {
        return cost;
    }

    /**
     * Sets the Spark cost for this <code>TextboxControl</code>.
     *
     * @param   cost
     *          The Spark cost for this <code>TextboxControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public TextboxControl setCost(Integer cost) {
        this.cost = cost;
        return this;
    }

    /**
     * Gets the submit button's text for this <code>TextboxControl</code>.
     *
     * @return  The submit button's text for this <code>TextboxControl</code>.
     *
     * @since   3.1.0
     */
    public String getSumbitText() {
        return sumbitText;
    }

    /**
     * Sets the submit text for this <code>TextboxControl</code>.
     *
     * @param   text
     *          The submit text for this <code>TextboxControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public TextboxControl setSumbitText(String text) {
        this.sumbitText = text;
        return this;
    }

    /**
     * Gets the placeholder text for this <code>TextboxControl</code>.
     *
     * @return  The placeholder text for this <code>TextboxControl</code>.
     *
     * @since   3.1.0
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Sets the placeholder text for this <code>TextboxControl</code>.
     *
     * @param   placeholder
     *          The placeholder text for this <code>TextboxControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public TextboxControl setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    /**
     * Shows if this <code>TextboxControl</code> has a submit button shown.
     *
     * @return  If this <code>TextboxControl</code> has a submit button shown.
     *
     * @since   3.1.0
     */
    public boolean hasSubmit() {
        return hasSubmit;
    }

    /**
     * Sets if this <code>TextboxControl</code> has a submit button shown.
     *
     * @param   hasSubmit
     *          If this <code>TextboxControl</code> has a submit button shown.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public TextboxControl setHasSubmit(boolean hasSubmit) {
        this.hasSubmit = hasSubmit;
        return this;
    }

    /**
     * Shows if this <code>TextboxControl</code> is multiline.
     *
     * @return  If this <code>TextboxControl</code> is multiline.
     *
     * @since   3.1.0
     */
    public boolean isMultiline() {
        return multiline;
    }

    /**
     * Sets if this <code>TextboxControl</code> is multiline.
     *
     * @param   multiline
     *          If this <code>TextboxControl</code> is multiline.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   3.1.0
     */
    public TextboxControl setMultiline(boolean multiline) {
        this.multiline = multiline;
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
    protected TextboxControl getThis() {
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
                if (o instanceof TextboxControl && this.equals(o)) {
                    this.meta = ((TextboxControl) o).meta;
                    this.setDisabled(((TextboxControl) o).isDisabled());
                    this.cost = ((TextboxControl) o).cost;
                    this.hasSubmit = ((TextboxControl) o).hasSubmit;
                    this.multiline = ((TextboxControl) o).multiline;
                    this.placeholder = ((TextboxControl) o).placeholder;
                    this.sumbitText = ((TextboxControl) o).sumbitText;
                    return true;
                }
            }
        }
        return false;
    }
}
