package com.mixer.interactive.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;

import java.util.Collection;

/**
 * The abstract class <code>InteractiveResource</code> is the superclass of all classes representing resources
 * on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @see         com.mixer.interactive.resources.control.InteractiveControl
 * @see         com.mixer.interactive.resources.participant.InteractiveParticipant
 * @see         com.mixer.interactive.resources.group.InteractiveGroup
 * @see         com.mixer.interactive.resources.scene.InteractiveScene
 *
 * @since       1.0.0
 */
public abstract class InteractiveResource <T extends InteractiveResource<T>> implements IInteractiveUpdatable<T> {

    /**
     * The etag for the resource
     */
    protected String etag;

    /**
     * Json object holding the map of meta properties
     */
    protected JsonObject meta;

    /**
     * Returns <code>this</code> as an instance of the specified generic. Utilizes the
     * <a target="_blank" href="http://www.angelikalanger.com/GenericsFAQ/FAQSections/ProgrammingIdioms.html#FAQ206">getThis</a>
     * programming idiom.
     *
     * @return  <code>this</code> as an object of type T
     *
     * @since   1.0.0
     */
    protected abstract T getThis();

    /**
     * Iterates through a <code>Collection</code> of <code>T</code> objects. If <code>this</code> is found to be
     * in the <code>Collection</code> then <code>this</code> has it's values updated.
     *
     * @param   objects
     *          A <code>Collection</code> of <code>T</code> objects
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public abstract T syncIfEqual(Collection<? extends T> objects);

    /**
     * Returns the etag for this resource.
     *
     * @return  The etag for this resource
     *
     * @since   1.0.0
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Returns the Json object holding the map of meta properties.
     *
     * @return  Json object holding the map of meta properties
     *
     * @since   1.0.0
     */
    public JsonObject getMeta() {
        return meta;
    }

    /**
     * Sets the Json object holding the map of meta properties.
     *
     * @param   meta
     *          Json object holding the map of meta properties
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public T setMeta(JsonObject meta) {
        this.meta = meta;
        return getThis();
    }

    /**
     * Adds a meta property to the meta property map object. If there already exists a meta property with the specified
     * key, it's value is overwritten.
     *
     * @param   key
     *          Meta property key
     * @param   value
     *          Meta property value
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public T addMetaProperty(String key, Object value) {
        if (meta == null) {
            meta = new JsonObject();
        }

        JsonObject metaObject;
        if (meta.has(key)) {
            metaObject = meta.get(key).getAsJsonObject();
        }
        else {
            metaObject = new JsonObject();
        }

        if (value instanceof JsonElement) {
            metaObject.add("value", (JsonElement) value);
        }
        else {
            metaObject.add("value", GameClient.GSON.toJsonTree(value));
        }
        meta.add(key, metaObject);
        return getThis();
    }
}
