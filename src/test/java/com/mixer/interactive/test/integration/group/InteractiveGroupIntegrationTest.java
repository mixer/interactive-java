package com.mixer.interactive.test.integration.group;

import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.event.group.GroupCreateEvent;
import com.mixer.interactive.event.group.GroupDeleteEvent;
import com.mixer.interactive.event.group.GroupUpdateEvent;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.resources.participant.InteractiveParticipant;
import com.mixer.interactive.resources.scene.InteractiveScene;
import com.mixer.interactive.test.util.TestEventHandler;
import com.mixer.interactive.test.util.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.mixer.interactive.GameClient.*;
import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests <code>InteractiveGroup</code> create/update/delete operations to the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveGroupIntegrationTest {

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
    public void can_get_groups() {
        try {
            Set<String> actualGroupIds = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups()).get()
                    .stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIds = new HashSet<>(Collections.singletonList("default"));
            Assert.assertEquals("The expected groups exist", expectedGroupIds, actualGroupIds);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_create_new_groups() {
        try {
            Set<String> actualGroupIds = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("awesome-group-1"), new InteractiveGroup("awesome-group-2")))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups())
                    .get()
                    .stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIds = new HashSet<>(Arrays.asList("default", "awesome-group-1", "awesome-group-2"));
            Assert.assertEquals("The expected groups exist", expectedGroupIds, actualGroupIds);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_create_group_in_non_default_scene() {
        try {
            Set<InteractiveGroup> actualGroups = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("awesome-group", "scene-1")))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups())
                    .get();
            Set<String> actualGroupIds = actualGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIds = new HashSet<>(Arrays.asList("default", "awesome-group"));
            Assert.assertEquals("The expected groups exist", expectedGroupIds, actualGroupIds);
            Assert.assertEquals("The new group is in the correct scene", true, actualGroups.stream().anyMatch(group -> "awesome-group".equals(group.getGroupID()) && "scene-1".equals(group.getSceneID())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_create_default_group() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("default")))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot create default group", 4009, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_create_group_in_non_existent_scenes() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("awesome-group", "awesome-scene")))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups())
                    .get();
            Assert.fail("Exception should be thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot create groups in scenes that do not exist", 4010, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_create_duplicate_groups() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("awesome-group", "scene-1")))
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("awesome-group", "scene-2")))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot create duplicate groups", 4009, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void can_update_group() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group");
            Set<InteractiveGroup> updatedGroups = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(group))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).update(group.addMetaProperty("awesome-property", "awesome-value")))
                    .get();
            Assert.assertEquals("Only one group was updated", 1, updatedGroups.size());
            Assert.assertEquals("Only the expected group was updated", "awesome-group", updatedGroups.iterator().next().getGroupID());
            Assert.assertNotEquals("The meta properties were changed", null, updatedGroups.iterator().next().getMeta());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_update_group_scene() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group", "scene-1");
            Set<InteractiveGroup> updatedGroups = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(group))
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups())
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).update(group.setScene("scene-2")))
                    .get();
            Assert.assertEquals("Only one group was updated", 1, updatedGroups.size());
            Assert.assertEquals("Only the expected group was updated", "awesome-group", updatedGroups.iterator().next().getGroupID());
            Assert.assertEquals("The group has the expected scene", "scene-2", updatedGroups.iterator().next().getSceneID());

            Set<InteractiveScene> actualScenes = gameClient.using(SCENE_SERVICE_PROVIDER).getScenes().get();
            Assert.assertEquals("The expected scene has the group", true,
                    actualScenes.stream().anyMatch(scene -> "scene-2".equals(scene.getSceneID()) && scene.getGroups().stream().anyMatch(group1 -> "awesome-group".equals(group1.getGroupID()))));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_update_non_existent_group() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).update(new InteractiveGroup("banana-group")))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot update a group that doesn't exist", 4008, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_update_group_into_non_existent_scene() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group", "scene-1");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(group))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).update(group.setScene("banana-scene")))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot update a group into a scene that doesn't exist", 4010, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void can_delete_group() {
        try {
            CompletableFuture<Boolean> testPromise = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("group-1")))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).delete("group-1"));
            Assert.assertEquals("Can delete group", true, testPromise.get());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_delete_group_and_reassign_to_other_group() {
        InteractiveGroup group1 = new InteractiveGroup("group-1");
        InteractiveGroup group2 = new InteractiveGroup("group-2");
        try {
            Set<InteractiveParticipant> testParticipants = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(group1, group2))
                    .thenCompose(groupsCreated -> TestUtils.TEST_PARTICIPANTS.get(0).connect())
                    .thenCompose(clientConnected -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .thenCompose(participants -> {
                        participants.forEach(participant -> participant.changeGroup(group1.getGroupID()));
                        return gameClient.using(PARTICIPANT_SERVICE_PROVIDER).update(participants);
                    })
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(participants -> gameClient.using(GROUP_SERVICE_PROVIDER).delete(group1.getGroupID(), group2.getGroupID()))
                    .thenCompose(groupDeleted -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get();
            Assert.assertEquals("Participants are in the expected group", true, testParticipants.stream().allMatch(participant -> group2.getGroupID().equals(participant.getGroupID())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_delete_default_group() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).delete("default"))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot delete the default group", 4018, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_delete_non_existent_group() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("awesome-group")))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).delete("not-so-awesome-group"))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot delete a non-existent group", 4008, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_delete_group_and_reassign_to_non_exitent_group() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("awesome-group")))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).delete("awesome-group", "not-so-awesome-group"))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot delete a non-existent group", 4008, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void can_self_create() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            Assert.assertEquals("Group was created", true, group.create(gameClient).get());

            boolean groupExists = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(group::equals)))
                    .get();
            Assert.assertEquals("Group exists", true, groupExists);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_self_update() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            InteractiveGroup originalGroup = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups().get().iterator().next();
            Assert.assertEquals("Group was updated", true, originalGroup.addMetaProperty("awesome-property", 4).update(gameClient).get());

            Set<InteractiveGroup> groups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups().get();
            Assert.assertEquals("Group has the updated property",
                    true,
                    groups.stream().anyMatch(group -> group.equals(originalGroup)
                            && ((JsonObject) group.getMeta().get("awesome-property")).get("value").getAsInt() == 4));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_self_delete() {
        try {
            InteractiveGroup group = new InteractiveGroup("awesome-group");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            Assert.assertEquals("Group was created", true, group.create(gameClient).get());

            boolean groupExists = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(group::equals)))
                    .get();
            Assert.assertEquals("Group exists", true, groupExists);

            Assert.assertEquals("Group was deleted", true, group.delete(gameClient).get());
            groupExists = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(group::equals)))
                    .get();
            Assert.assertEquals("Group does not exists", false, groupExists);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void group_create_event_posted() {
        try {
            InteractiveGroup testGroup = new InteractiveGroup("bacon-group");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(testGroup))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("A create group event was posted", true, TestEventHandler.HANDLER.getEvents().stream()
                    .anyMatch(event -> event instanceof GroupCreateEvent && ((GroupCreateEvent) event).getGroups().contains(testGroup)));
            Assert.assertEquals("Only one create group event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> event instanceof GroupCreateEvent && ((GroupCreateEvent) event).getGroups().contains(testGroup))
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void group_update_event_posted() {
        try {
            InteractiveGroup group = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).getGroups()).get().iterator().next();

            group.addMetaProperty("awesome_property", "awesome_value");
            gameClient.using(GROUP_SERVICE_PROVIDER).update(group)
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("An update group event was posted for the expected group", true,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .anyMatch(event -> event instanceof GroupUpdateEvent && ((GroupUpdateEvent) event).getGroups().stream().anyMatch(updatedGroup -> group.getGroupID().equals(updatedGroup.getGroupID()))));
            Assert.assertEquals("Only one update group event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> event instanceof GroupUpdateEvent && ((GroupUpdateEvent) event).getGroups().stream().anyMatch(updatedGroup -> group.getGroupID().equals(updatedGroup.getGroupID())))
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void group_delete_event_posted() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("bacon-group")))
                    .thenCompose(created -> gameClient.using(GROUP_SERVICE_PROVIDER).delete("bacon-group"))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("A delete group event was posted for the expected group", true,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .anyMatch(event -> event instanceof GroupDeleteEvent && ((GroupDeleteEvent) event).getGroupID().equals("bacon-group")));
            Assert.assertEquals("Only one delete group event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> event instanceof GroupDeleteEvent && ((GroupDeleteEvent) event).getGroupID().equals("bacon-group"))
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
