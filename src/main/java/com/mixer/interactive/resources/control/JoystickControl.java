package com.mixer.interactive.resources.control;

import com.google.common.base.Objects;
import com.mixer.interactive.resources.InteractiveResource;

import java.util.Collection;

/**
 * A <code>InteractiveControl</code> represents a joystick control on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class JoystickControl extends InteractiveControl<JoystickControl> {

    /**
     * The rate at which <code>move</code> events are to be sampled from the Interactive service
     */
    private Integer sampleRate;

    /**
     * The location of the "halo" effect on screen. Should be given in the range [0, 2)
     */
    private Number angle;

    /**
     * The opacity of the "halo" effect
     */
    private Number intensity;

    /**
     * Initializes a new <code>JoystickControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>JoystickControl</code>
     *
     * @since   1.0.0
     */
    public JoystickControl(String controlID) {
        this(controlID, null, null, new InteractiveControlPosition[0]);
    }

    /**
     * Initializes a new <code>JoystickControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>JoystickControl</code>
     * @param   sceneID
     *          Unique identifier for the <code>InteractiveScene</code> that contains this control
     * @param   etag
     *          The etag for the <code>JoystickControl</code>
     * @param   position
     *          An array of initial <code>InteractiveControlPosition</code> for this control
     *
     * @since   1.0.0
     */
    public JoystickControl(String controlID, String sceneID, String etag, InteractiveControlPosition ... position) {
        super(controlID, sceneID, etag, InteractiveControlType.JOYSTICK, position);
    }

    /**
     * Returns the sample rate for this <code>JoystickControl</code>.
     *
     * @return  The sample rate for this <code>JoystickControl</code>
     *
     * @since   1.0.0
     */
    public Integer getSampleRate() {
        return sampleRate;
    }

    /**
     * Sets the sample rate for this <code>JoystickControl</code>.
     *
     * @param   sampleRate
     *          The sample rate for this <code>JoystickControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public JoystickControl setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
        return getThis();
    }

    /**
     * Returns the location of the "halo" effect on screen.
     *
     * @return  The location of the "halo" effect on screen
     *
     * @since   1.0.0
     */
    public Number getAngle() {
        return angle;
    }

    /**
     * Sets the location of the "halo" effect on screen. Should be given in the range [0, 2).
     *
     * @param   angle
     *          The location of the "halo" effect on screen
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public JoystickControl setAngle(Number angle) {
        this.angle = angle;
        return getThis();
    }

    /**
     * Returns the opacity of the "halo" effect.
     *
     * @return  The opacity of the "halo" effect
     *
     * @since   1.0.0
     */
    public Number getIntensity() {
        return intensity;
    }

    /**
     * Sets the opacity of the "halo" effect.
     *
     * @param   intensity
     *          The opacity of the "halo" effect
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public JoystickControl setIntensity(Number intensity) {
        this.intensity = intensity;
        return getThis();
    }

    /**
     * {@inheritDoc}
     *
     * @see     InteractiveResource#getThis()
     *
     * @since   1.0.0
     */
    @Override
    protected JoystickControl getThis() {
        return this;
    }

    /**
     * Iterates through a <code>Collection</code> of <code>JoystickControls</code>. If <code>this</code> is found to be
     * in the <code>Collection</code> then <code>this</code> has it's values updated.
     *
     * @param   objects
     *          A <code>Collection</code> of <code>JoystickControls</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @see     InteractiveResource#syncIfEqual(Collection)
     *
     * @since   1.0.0
     */
    @Override
    public JoystickControl syncIfEqual(Collection<? extends JoystickControl> objects) {
        if (objects != null) {
            for (JoystickControl object : objects) {
                if (this.equals(object)) {
                    this.meta = object.meta;
                    this.etag = object.etag;
                    this.setDisabled(object.isDisabled());
                    this.sampleRate = object.sampleRate;
                    this.angle = object.angle;
                    this.intensity = object.intensity;
                }
            }
        }
        return getThis();
    }

    /**
     * Returns a <code>String</code> representation of this <code>JoystickControl</code>.
     *
     * @return  <code>String</code> representation of this <code>JoystickControl</code>
     *
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("controlID", getControlID())
                .add("sceneID", getSceneID())
                .add("etag", getEtag())
                .add("kind", getKind())
                .add("disabled", isDisabled())
                .add("sampleRate", sampleRate)
                .add("angle", angle)
                .add("intensity", intensity)
                .add("meta", getMeta())
                .toString();
    }
}
