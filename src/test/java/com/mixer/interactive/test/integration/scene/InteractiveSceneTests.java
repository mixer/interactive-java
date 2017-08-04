package com.mixer.interactive.test.integration.scene;

import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveException;
import com.mixer.interactive.exception.InteractiveNoHostsFoundException;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.resources.control.ButtonControl;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.scene.InteractiveScene;
import com.mixer.interactive.test.util.TestEventHandler;
import com.mixer.interactive.util.EndpointUtil;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.mixer.interactive.GameClient.SCENE_SERVICE_PROVIDER;
import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests <code>InteractiveScene</code> create/update/delete operations to the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InteractiveSceneTests {

    /**
     * URI for Interactive host being tested against
     */
    private static URI interactiveHost;

    /**
     * <code>GameClient</code> that connects to an Interactive integration that contains the default scene with no
     * controls
     */
    private static GameClient emptyGameClient;

    /**
     * <code>GameClient</code> that connects to an Interactive integration that contains the default scene with controls
     */
    private static GameClient singleSceneGameClient;

    /**
     * <code>GameClient</code> that connects to an Interactive integration that contains multiple scenes that all
     * contain controls
     */
    private static GameClient multiSceneGameClient;

    @BeforeClass
    public static void setup() throws InteractiveNoHostsFoundException {
        interactiveHost = INTERACTIVE_LOCALHOST;
        if (!TEST_LOCAL) {
            interactiveHost = EndpointUtil.getInteractiveHost().getAddress();
        }
    }

    @Before
    public void setupGameClient() {
        emptyGameClient = new GameClient(EMPTY_INTERACTIVE_PROJECT);
        singleSceneGameClient = new GameClient(SINGLE_SCENE_INTERACTIVE_PROJECT);
        multiSceneGameClient = new GameClient(MULTIPLE_SCENES_INTERACTIVE_PROJECT);

        emptyGameClient.getEventBus().register(TestEventHandler.HANDLER);
        singleSceneGameClient.getEventBus().register(TestEventHandler.HANDLER);
        multiSceneGameClient.getEventBus().register(TestEventHandler.HANDLER);
    }

    @After
    public void teardownGameClient() {
        TestEventHandler.HANDLER.clear();

        emptyGameClient.disconnect();
        singleSceneGameClient.disconnect();
        multiSceneGameClient.disconnect();

        emptyGameClient = null;
        singleSceneGameClient = null;
        multiSceneGameClient = null;
    }

    // GameClient#getScenes
    @Test
    public void getScenes_valid_single_scene_project() {
        try {
            singleSceneGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            Set<InteractiveScene> scenes = singleSceneGameClient.using(SCENE_SERVICE_PROVIDER).getScenes();
            Assert.assertEquals("Only one scene exists", 1, scenes.size());
            Assert.assertEquals("Only the default scene exists", "default", scenes.iterator().next().getSceneID());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void getScenes_valid_multiple_scene_project() {
        try {
            multiSceneGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            Set<String> actual = multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).getScenes().stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet());
            Set<String> expected = new HashSet<>(Arrays.asList("default", "scene-1", "scene-2"));
            Assert.assertEquals("Only the expected scenes exist", expected, actual);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    // GameClient#createScenes
    @Test
    public void createScenes_valid_empty_scene() {
        try {
            emptyGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            emptyGameClient.using(SCENE_SERVICE_PROVIDER).createScenes(new InteractiveScene("test-scene"));
            Set<String> actual = emptyGameClient.using(SCENE_SERVICE_PROVIDER).getScenes().stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet());
            Set<String> expected = new HashSet<>(Arrays.asList("default", "test-scene"));
            Assert.assertEquals("Only the expected scenes exist", expected, actual);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createScenes_valid_multiple_scenes() {
        try {
            emptyGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            emptyGameClient.using(SCENE_SERVICE_PROVIDER).createScenes(new InteractiveScene("test-scene-1"), new InteractiveScene("test-scene-2"), new InteractiveScene("test-scene-3"));
            Set<String> actual = emptyGameClient.using(SCENE_SERVICE_PROVIDER).getScenes().stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet());
            Set<String> expected = new HashSet<>(Arrays.asList("default", "test-scene-1", "test-scene-2", "test-scene-3"));
            Assert.assertEquals(expected, actual);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createScenes_valid_scene_with_controls() {
        try {
            emptyGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            InteractiveScene scene = new InteractiveScene("test-scene");
            scene.addControls(new ButtonControl("test-button-1"), new ButtonControl("test-button-2"));
            Set<InteractiveScene> createdScenes = emptyGameClient.using(SCENE_SERVICE_PROVIDER).createScenes(scene);

            // Test scene was created
            Assert.assertEquals("Only one scene was created", 1, createdScenes.size());
            Assert.assertEquals("Only the expect scene was created", "test-scene", createdScenes.iterator().next().getSceneID());

            // Test scenes are what we expect
            Set<InteractiveScene> actualScenes = emptyGameClient.using(SCENE_SERVICE_PROVIDER).getScenes();
            Set<String> actualSceneIDs = actualScenes.stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet());
            Set<String> expectedSceneIDs = new HashSet<>(Arrays.asList("default", "test-scene"));
            Assert.assertEquals(expectedSceneIDs, actualSceneIDs);

            // Test controls were created as expected
            InteractiveScene createdScene = createdScenes.iterator().next();
            Set<String> createdControlIDs = createdScene.getControls().stream().map(InteractiveControl::getControlID).collect(Collectors.toSet());
            Set<String> expectedControlIDs = new HashSet<>(Arrays.asList("test-button-1", "test-button-2"));
            Assert.assertEquals(expectedControlIDs, createdControlIDs);

            Set<String> createdControlSceneIDs = createdScene.getControls().stream().map(InteractiveControl::getSceneID).collect(Collectors.toSet());
            Set<String> expectedControlSceneIDs = new HashSet<>(Collections.singletonList("test-scene"));
            Assert.assertEquals(expectedControlSceneIDs, createdControlSceneIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createScenes_invalid_default_scene() {
        try {
            emptyGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            emptyGameClient.using(SCENE_SERVICE_PROVIDER).createScenes(new InteractiveScene("default"));
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot create a duplicate of the default scene", 4011, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail();
        }
    }

    @Test
    public void createScenes_invalid_scene_already_exists() {
        try {
            emptyGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            emptyGameClient.using(SCENE_SERVICE_PROVIDER).createScenes(new InteractiveScene("test-scene"), new InteractiveScene("test-scene"));
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot create a duplicate scene", 4011, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail();
        }
    }

    // GameClient#updateScenes
    @Test
    public void updateScenes_valid_add_meta() {
        try {
            emptyGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            InteractiveScene defaultScene = emptyGameClient.using(SCENE_SERVICE_PROVIDER).getScenes().iterator().next();
            JsonObject originalMeta = (JsonObject) defaultScene.getMeta();
            defaultScene.addMetaProperty("awesome_property", "awesome_value");
            Set<InteractiveScene> updatedScenes = emptyGameClient.using(SCENE_SERVICE_PROVIDER).updateScenes(defaultScene);

            Assert.assertEquals(1, updatedScenes.size());
            Assert.assertEquals("default", updatedScenes.iterator().next().getSceneID());

            InteractiveScene updatedScene = updatedScenes.iterator().next();
            Assert.assertNotEquals("Updated scene has different meta properties than before", originalMeta, updatedScene.getMeta());
            Assert.assertEquals("Updated scene has the new meta property", "awesome_value", ((JsonObject) updatedScene.getMeta()).get("awesome_property").getAsJsonObject().get("value").getAsString());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateScenes_invalid_non_existent_scene() {
        try {
            singleSceneGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            singleSceneGameClient.using(SCENE_SERVICE_PROVIDER).updateScenes(new InteractiveScene("banana-scene"));
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot update non-existent scene", 4010, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    // GameClient#deleteScene
    @Test
    public void deleteScene_valid_default_reassignment() {
        try {
            multiSceneGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).deleteScene("scene-1");

            Set<InteractiveScene> actualScenes = multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).getScenes();
            Set<String> actualSceneIDs = actualScenes.stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet());
            Set<String> expectedSceneIDs = new HashSet<>(Arrays.asList("default", "scene-2"));
            Assert.assertEquals(expectedSceneIDs, actualSceneIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void deleteScene_valid_reassign_to_default() {
        try {
            multiSceneGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).getScenes().forEach(interactiveScene -> System.out.println(interactiveScene.getSceneID()));
            multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).deleteScene("scene-1", "default");

            Set<InteractiveScene> actualScenes = multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).getScenes();
            Set<String> actualSceneIDs = actualScenes.stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet());
            Set<String> expectedSceneIDs = new HashSet<>(Arrays.asList("default", "scene-2"));
            Assert.assertEquals(expectedSceneIDs, actualSceneIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void deleteScene_valid_delete_empty_scene() {
        try {
            multiSceneGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).deleteScene("scene-2");
            Set<InteractiveScene> actualScenes = multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).getScenes();
            Set<String> actualSceneIDs = actualScenes.stream().map(InteractiveScene::getSceneID).collect(Collectors.toSet());
            Set<String> expectedSceneIDs = new HashSet<>(Arrays.asList("default", "scene-1"));
            Assert.assertEquals(expectedSceneIDs, actualSceneIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void deleteScene_invalid_delete_default_scene() {
        try {
            multiSceneGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).deleteScene("default");
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot delete the default scene", 4018, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail();
        }
    }

    @Test
    public void deleteScene_invalid_reassign_to_non_existent_scene() {
        try {
            multiSceneGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).deleteScene("scene-1", "scene-banana");
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot reassign groups to a non-existent scene", 4010, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail();
        }
    }

    @Test
    public void deleteScene_invalid_reassign_to_same_scene() {
        try {
            multiSceneGameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
            multiSceneGameClient.using(SCENE_SERVICE_PROVIDER).deleteScene("scene-1", "scene-1");
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot reassign groups to the scene that was just deleted", 4010, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail();
        }
    }
}
