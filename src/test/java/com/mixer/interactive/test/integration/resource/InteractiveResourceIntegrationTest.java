package com.mixer.interactive.test.integration.resource;

import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.test.util.TestEventHandler;
import com.mixer.interactive.test.util.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.mixer.interactive.GameClient.GROUP_SERVICE_PROVIDER;
import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests <code>InteractiveResource</code> changes to meta properties.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.0.0
 */
public class InteractiveResourceIntegrationTest {

    /**
     * <code>GameClient</code> that connects to an Interactive integration that contains multiple scenes that all
     * contain controls
     */
    private static GameClient gameClient;

    @Before
    public void setupGameClient() {
        gameClient = new GameClient(INTERACTIVE_PROJECT_ID, TestUtils.CLIENT_ID);
        gameClient.getEventBus().register(TestEventHandler.HANDLER);
    }

    @After
    public void teardownGameClient() {
        TestEventHandler.HANDLER.clear();
        gameClient.disconnect();
        gameClient = null;
    }

    @Test
    public void can_create_with_meta_properties() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group")
                    .addMetaProperty("awesome-property", "awesome-value");
            Set<InteractiveGroup> groups = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(group))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups())
                    .get();

            Assert.assertEquals("The resource was created",
                    new HashSet<>(Collections.singletonList(group.getGroupID())),
                    groups.stream().filter(group1 -> !"default".equals(group1.getGroupID())).map(InteractiveGroup::getGroupID).collect(Collectors.toSet()));
            Assert.assertEquals("The resource has the expected meta properties",
                    true,
                    groups.stream().anyMatch(interactiveGroup -> group.getGroupID().equals(interactiveGroup.getGroupID()) && ((JsonObject) group.getMeta().get("awesome-property")).get("value").getAsString().equals("awesome-value")));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_add_a_meta_property() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group");
            Set<InteractiveGroup> updatedGroups = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(group))
                    .thenCompose(created -> {
                        group.addMetaProperty("awesome-property", "awesome-value");
                        return gameClient.using(GROUP_SERVICE_PROVIDER).update(group);
                    })
                    .get();

            Assert.assertEquals("Only the expected resource was updated",
                    new HashSet<>(Collections.singletonList(group.getGroupID())),
                    updatedGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet()));
            Assert.assertEquals("The resource has the expected meta properties",
                    true,
                    updatedGroups.stream().anyMatch(interactiveGroup -> group.getGroupID().equals(interactiveGroup.getGroupID()) && ((JsonObject) group.getMeta().get("awesome-property")).get("value").getAsString().equals("awesome-value")));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_change_a_meta_property() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group")
                    .addMetaProperty("awesome-property", "awesome-value")
                    .addMetaProperty("other-property", "other-value");
            Set<InteractiveGroup> groups = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(group))
                    .thenCompose(created -> {
                        JsonObject metaObject = group.getMeta();
                        metaObject.addProperty("other-property", 4);
                        group.setMeta(metaObject);
                        return gameClient.using(GROUP_SERVICE_PROVIDER).update(group);
                    })
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups())
                    .get();

            Assert.assertEquals("The resource was updated",
                    new HashSet<>(Collections.singletonList(group.getGroupID())),
                    groups.stream().filter(group1 -> !"default".equals(group1.getGroupID())).map(InteractiveGroup::getGroupID).collect(Collectors.toSet()));
            Assert.assertEquals("The resource has the expected meta properties",
                    true,
                    groups.stream().anyMatch(updatedGroup -> group.getGroupID().equals(updatedGroup.getGroupID()) && group.getMeta().get("other-property").getAsNumber().equals(4)));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_remove_a_meta_property() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group")
                    .addMetaProperty("awesome-property", "awesome-value")
                    .addMetaProperty("other-property", "other-value");
            Set<InteractiveGroup> groups = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(group))
                    .thenCompose(created -> {
                        JsonObject metaObject = group.getMeta();
                        metaObject.remove("other-property");
                        group.setMeta(metaObject);
                        return gameClient.using(GROUP_SERVICE_PROVIDER).update(group);
                    })
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups())
                    .get();

            Assert.assertEquals("The resource was updated",
                    new HashSet<>(Collections.singletonList(group.getGroupID())),
                    groups.stream().filter(group1 -> !"default".equals(group1.getGroupID())).map(InteractiveGroup::getGroupID).collect(Collectors.toSet()));
            Assert.assertEquals("The resource has the expected meta properties",
                    true,
                    groups.stream().anyMatch(updatedGroup -> group.getGroupID().equals(updatedGroup.getGroupID()) && ((JsonObject) group.getMeta().get("awesome-property")).get("value").getAsString().equals("awesome-value")));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_remove_all_meta_properties() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group")
                    .addMetaProperty("awesome-property", "awesome-value");
            Set<InteractiveGroup> groups = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(group))
                    .thenCompose(created -> {
                        group.setMeta(null);
                        return gameClient.using(GROUP_SERVICE_PROVIDER).update(group);
                    })
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups())
                    .get();

            Assert.assertEquals("The resource was updated",
                    new HashSet<>(Collections.singletonList(group.getGroupID())),
                    groups.stream().filter(group1 -> !"default".equals(group1.getGroupID())).map(InteractiveGroup::getGroupID).collect(Collectors.toSet()));
            Assert.assertEquals("The resource has the expected meta properties",
                    true,
                    groups.stream().anyMatch(updatedGroup -> group.getGroupID().equals(updatedGroup.getGroupID()) && group.getMeta() == null));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }
}
