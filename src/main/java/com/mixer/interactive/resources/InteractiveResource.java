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
public abstract class InteractiveResource<T extends InteractiveResource<T>> implements IInteractiveUpdatable {

    /**
     * Json object holding the map of meta properties
     */
    protected JsonElement meta;

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
     * Iterates through a <code>Collection</code> of Objects. If <code>this</code> is found to be in the
     * <code>Collection</code> then <code>this</code> has it's values updated.
     *
     * @param   objects
     *          A <code>Collection</code> of Objects
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          provided <code>Collection</code> contains <code>this</code>
     *
     * @since   1.0.0
     */
    public abstract boolean syncIfEqual(Collection<?> objects);

    /**
     * Returns the Json object holding the map of meta properties.
     *
     * @return  Json object holding the map of meta properties
     *
     * @since   1.0.0
     */
    public JsonObject getMeta() {
        if (meta instanceof JsonObject) {
            return (JsonObject) meta;
        }
        return null;
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
        if (!(meta instanceof JsonObject)) {
            meta = new JsonObject();
        }

        JsonObject metaObject = (JsonObject) meta;
        JsonObject propertyObject;
        if (metaObject.has(key)) {
            propertyObject = metaObject.get(key).getAsJsonObject();
        }
        else {
            propertyObject = new JsonObject();
        }

        if (value instanceof JsonElement) {
            propertyObject.add("value", (JsonElement) value);
        }
        else {
            propertyObject.add("value", GameClient.GSON.toJsonTree(value));
        }
        metaObject.add(key, propertyObject);
        return getThis();
    }
}
