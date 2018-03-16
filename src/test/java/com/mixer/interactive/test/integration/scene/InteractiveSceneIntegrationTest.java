package com.mixer.interactive.test.integration.scene;

import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.event.scene.SceneCreateEvent;
import com.mixer.interactive.event.scene.SceneDeleteEvent;
import com.mixer.interactive.event.scene.SceneUpdateEvent;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.resources.control.ButtonControl;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.group.InteractiveGroup;
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

import static com.mixer.interactive.GameClient.GROUP_SERVICE_PROVIDER;
import static com.mixer.interactive.GameClient.SCENE_SERVICE_PROVIDER;
import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests <code>InteractiveScene</code> create/update/delete operations to the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveSceneIntegrationTest {

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
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void can_get_scenes() {
        try {
            Set<String> actualScenes = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(aVoid -> gameClient.using(SCENE_SERVICE_PROVIDER).getScenes())
                    .get().stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet());
            Set<String> expectedScenes = new HashSet<>(Arrays.asList("default", "scene-1", "scene-2"));
            Assert.assertEquals("Only the expected scenes exist", expectedScenes, actualScenes);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_create_scene_without_controls() {
        try {
            Set<String> actualScenes = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connect -> gameClient.using(SCENE_SERVICE_PROVIDER).create(new InteractiveScene("test-scene-1"), new InteractiveScene("test-scene-2")))
                    .thenCompose(scenes -> gameClient.using(SCENE_SERVICE_PROVIDER).getScenes())
                    .thenCompose(scenes -> CompletableFuture.completedFuture(
                            scenes.stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet()))
                    )
                    .get();
            Set<String> expectedScenes = new HashSet<>(Arrays.asList("default", "scene-1", "scene-2", "test-scene-1", "test-scene-2"));
            Assert.assertEquals("Only the expected scenes exist", expectedScenes, actualScenes);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_create_scene_with_controls() {
        try {
            InteractiveScene testScene = new InteractiveScene("test-scene");
            testScene.getControls().add(new ButtonControl("test-button-1"));
            testScene.getControls().add(new ButtonControl("test-button-2"));

            Set<InteractiveScene> createdScenes = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).create(testScene))
                    .get();
            Assert.assertEquals("Only one scene was created", 1, createdScenes.size());
            Assert.assertEquals("Only the expect scene was created", "test-scene", createdScenes.iterator().next().getSceneID());

            InteractiveScene createdScene = createdScenes.iterator().next();
            Set<String> createdControlIds = createdScene.getControls().stream().map(InteractiveControl::getControlID).collect(Collectors.toSet());
            Set<String> expectedControlIds = new HashSet<>(Arrays.asList("test-button-1", "test-button-2"));
            Assert.assertEquals("The expected controls were created with the scene", expectedControlIds, createdControlIds);

            Set<String> createdControlsSceneIds = createdScene.getControls().stream().map(InteractiveControl::getSceneID).collect(Collectors.toSet());
            Set<String> expectedControlsSceneId = new HashSet<>(Collections.singletonList("test-scene"));
            Assert.assertEquals("Created controls have the expected scene id", expectedControlsSceneId, createdControlsSceneIds);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }

        CompletableFuture<Set<String>> getScenesPromise = gameClient.using(SCENE_SERVICE_PROVIDER).getScenes()
                .thenCompose(scenes -> CompletableFuture.completedFuture(scenes.stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet())));

        Set<String> expectedScenes = new HashSet<>(Arrays.asList("default", "scene-1", "scene-2", "test-scene"));

        try {
            Assert.assertEquals("The expected scenes exist after scene creation", expectedScenes, getScenesPromise.get());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_create_default_scene() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).create(new InteractiveScene("default")))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot create a duplicate of the default scene", 4011, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_create_duplicate_scene() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).create(new InteractiveScene("scene-1")))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot create a duplicate scene", 4011, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void can_update_scene() {
        try {
            InteractiveScene testScene = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).getScenes())
                    .get().iterator().next();
            Assert.assertEquals("Scene does not have any meta properties", true, testScene.getMeta() == null || testScene.getMeta().entrySet().isEmpty());
            testScene.addMetaProperty("awesome_property", "awesome_value");
            CompletableFuture<Set<InteractiveScene>> updatePromise = gameClient.using(SCENE_SERVICE_PROVIDER).update(testScene);
            Assert.assertEquals("Only one scene was updated", 1, updatePromise.get().size());
            Assert.assertEquals("Only the expected scene was updated", testScene.getSceneID(), updatePromise.get().iterator().next().getSceneID());

            InteractiveScene updatedScene = updatePromise.get().iterator().next();
            Assert.assertEquals("Updated scene has the new meta property", "awesome_value", ((JsonObject) updatedScene.getMeta().get("awesome_property")).get("value").getAsString());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_update_non_existent_scene() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).update(new InteractiveScene("banana-scene")))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot update non-existent scene", 4010, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void can_delete_scene() {
        try {
            Set<String> actualScenes = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).delete("scene-1"))
                    .thenCompose(deleted -> gameClient.using(SCENE_SERVICE_PROVIDER).getScenes())
                    .thenCompose(scenes -> CompletableFuture.completedFuture(
                            scenes.stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet()))
                    )
                    .get();

            Set<String> expectedScenes = new HashSet<>(Arrays.asList("default", "scene-2"));
            Assert.assertEquals("Scenes can be deleted", expectedScenes, actualScenes);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_delete_scene_and_reassign_groups() {
        try {
            Set<InteractiveScene> scenes = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("cool-group", "scene-1")))
                    .thenCompose(deleted -> gameClient.using(SCENE_SERVICE_PROVIDER).getScenes())
                    .thenCompose(groupCreated -> gameClient.using(SCENE_SERVICE_PROVIDER).delete("scene-1", "scene-2"))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(deleted -> gameClient.using(SCENE_SERVICE_PROVIDER).getScenes())
                    .get();

            Set<String> actualSceneIds = scenes.stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet());
            Set<String> expectedSceneIds = new HashSet<>(Arrays.asList("default", "scene-2"));
            Assert.assertEquals("Can delete scenes and reassign groups on the scene to default scene", expectedSceneIds, actualSceneIds);

            Set<InteractiveGroup> groups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups().get().stream()
                    .filter(group -> "cool-group".equals(group.getGroupID())).collect(Collectors.toSet());
            Assert.assertEquals("Only one group matches the groupId", 1, groups.size());
            Assert.assertEquals("The group is in the correct scene","scene-2", groups.iterator().next().getSceneID());

            Set<InteractiveScene> scenesContainingGroup = scenes.stream().filter(scene -> {
                for (InteractiveGroup group : scene.getGroups()) {
                    if ("cool-group".equals(group.getGroupID())) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toSet());
            Assert.assertEquals("Only one scene has the group", 1, scenesContainingGroup.size());
            Assert.assertEquals("The scene containing the group is the expected one", "scene-2", scenesContainingGroup.iterator().next().getSceneID());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_delete_default_scene() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).delete("default"))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot delete the default scene", 4018, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_delete_non_existent_scene() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).delete("banana-scene"))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot delete non-existent scene", 4010, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_delete_scene_and_reassign_groups_to_non_existent_scene() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("cool-group", "scene-1")))
                    .thenCompose(groupCreated -> gameClient.using(SCENE_SERVICE_PROVIDER).delete("scene-1", "banana-scene"))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot delete a valid scene and reassign groups to a non-existent scene", 4010, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_delete_scene_and_reassign_to_self() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("cool-group", "scene-1")))
                    .thenCompose(groupCreated -> gameClient.using(SCENE_SERVICE_PROVIDER).delete("scene-1", "scene-1"))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot reassign groups to a scene being deleted", 4010, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void can_delete_scene_and_recreate_it() {
        try {
            Set<InteractiveScene> scenes = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).getScenes())
                    .get();
            Assert.assertEquals("Scenes are already present", false, scenes.isEmpty());

            InteractiveScene scene = scenes.stream().filter(aScene -> !"default".equals(aScene.getSceneID())).collect(Collectors.toSet()).iterator().next();
            Assert.assertEquals("A non-default scene exists", true, scene != null);

            assert scene != null;
            CompletableFuture<Set<InteractiveScene>> testPromise = gameClient.using(SCENE_SERVICE_PROVIDER).delete(scene.getSceneID())
                    .thenCompose(deleted -> gameClient.using(SCENE_SERVICE_PROVIDER).create(scene));
            Assert.assertEquals("Only one scene was created", 1, testPromise.get().size());
            Assert.assertEquals("Only the expected scene was created", scene.getSceneID(), testPromise.get().iterator().next().getSceneID());
            Assert.assertEquals("Same number of scenes exist as at start", scenes.size(), gameClient.using(SCENE_SERVICE_PROVIDER).getScenes().get().size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_self_create() {
        try {
            InteractiveScene scene = new InteractiveScene("awesome-scene");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            Assert.assertEquals("Scene was created", true, scene.create(gameClient).get());

            boolean sceneExists = gameClient.using(SCENE_SERVICE_PROVIDER).getScenes()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(scene::equals)))
                    .get();
            Assert.assertEquals("Scene exists", true, sceneExists);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_self_update() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            InteractiveScene originalScene = gameClient.using(SCENE_SERVICE_PROVIDER).getScenes().get().iterator().next();
            originalScene.addMetaProperty("awesome-property", 4);
            Assert.assertEquals("Scene was updated", true, originalScene.update(gameClient).get());

            Set<InteractiveScene> scenes = gameClient.using(SCENE_SERVICE_PROVIDER).getScenes().get();
            Assert.assertEquals("Scene has the updated property",
                    true,
                    scenes.stream().anyMatch(scene -> originalScene.getSceneID().equals(scene.getSceneID())
                            && ((JsonObject) originalScene.getMeta().get("awesome-property")).get("value").getAsInt() == 4));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_self_delete() {
        try {
            InteractiveScene scene = new InteractiveScene("awesome-scene");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            Assert.assertEquals("Scene was created", true, scene.create(gameClient).get());

            boolean sceneExists = gameClient.using(SCENE_SERVICE_PROVIDER).getScenes()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(scene::equals)))
                    .get();
            Assert.assertEquals("Scene exists", true, sceneExists);

            Assert.assertEquals("Scene was deleted", true, scene.delete(gameClient).get());
            sceneExists = gameClient.using(SCENE_SERVICE_PROVIDER).getScenes()
                    .thenCompose(scenes -> CompletableFuture.supplyAsync(() -> scenes.stream().anyMatch(scene::equals)))
                    .get();
            Assert.assertEquals("Scene does not exists", false, sceneExists);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void scene_create_event_posted() {
        try {
            InteractiveScene scene = new InteractiveScene("bacon-scene");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).create(scene))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("A create scene event was posted", true, TestEventHandler.HANDLER.getEvents().stream()
                    .anyMatch(event -> event instanceof SceneCreateEvent && ((SceneCreateEvent) event).getScenes().stream().anyMatch(eventScene -> scene.getSceneID().equals(eventScene.getSceneID()))));
            Assert.assertEquals("Only one create scene event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> event instanceof SceneCreateEvent && ((SceneCreateEvent) event).getScenes().stream().anyMatch(eventScene -> scene.getSceneID().equals(eventScene.getSceneID())))
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void scene_update_event_posted() {
        try {
            InteractiveScene scene = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).getScenes()).get().iterator().next();

            scene.addMetaProperty("awesome_property", "awesome_value");
            gameClient.using(SCENE_SERVICE_PROVIDER).update(scene)
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("An update scene event was posted for the expected scene", true,
                    TestEventHandler.HANDLER.getEvents().stream()
                        .anyMatch(event -> event instanceof SceneUpdateEvent && ((SceneUpdateEvent) event).getScenes().stream().anyMatch(eventScene -> scene.getSceneID().equals(eventScene.getSceneID()))));
            Assert.assertEquals("Only one update scene event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> event instanceof SceneUpdateEvent && ((SceneUpdateEvent) event).getScenes().stream().anyMatch(eventScene -> scene.getSceneID().equals(eventScene.getSceneID())))
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void scene_delete_event_posted() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(SCENE_SERVICE_PROVIDER).delete("scene-1"))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("A delete scene event was posted for the expected scene", true,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .anyMatch(event -> event instanceof SceneDeleteEvent && ((SceneDeleteEvent) event).getSceneID().equals("scene-1")));
            Assert.assertEquals("Only one delete scene event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> event instanceof SceneDeleteEvent && ((SceneDeleteEvent) event).getSceneID().equals("scene-1"))
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
