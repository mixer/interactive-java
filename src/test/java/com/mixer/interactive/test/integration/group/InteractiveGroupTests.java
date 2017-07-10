package com.mixer.interactive.test.integration.group;

import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveException;
import com.mixer.interactive.exception.InteractiveNoHostsFoundException;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.test.util.TestEventHandler;
import com.mixer.interactive.util.EndpointUtil;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mixer.interactive.GameClient.GROUP_SERVICE_PROVIDER;
import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests <code>InteractiveGroup</code> create/update/delete operations to the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InteractiveGroupTests {

    /**
     * URI for Interactive host being tested against
     */
    private static URI interactiveHost;

    /**
     * <code>GameClient</code> that connects to an Interactive integration that contains multiple scenes that all
     * contain controls
     */
    private static GameClient gameClient;

    @BeforeClass
    public static void setup() throws InteractiveNoHostsFoundException {
        interactiveHost = INTERACTIVE_LOCALHOST;
        if (!TEST_LOCAL) {
            interactiveHost = EndpointUtil.getInteractiveHost().getAddress();
        }
    }

    @Before
    public void setupGameClient() {
        gameClient = new GameClient(MULTIPLE_SCENES_INTERACTIVE_PROJECT);
        gameClient.getEventBus().register(TestEventHandler.HANDLER);
        gameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
    }

    @After
    public void teardownGameClient() {
        TestEventHandler.HANDLER.clear();
        gameClient.disconnect();
        gameClient = null;
    }

    // GameClient#getGroups
    @Test
    public void getGroups_valid_default() {
        try {
            Set<String> actualGroupIDs = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups().stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default"));
            Assert.assertEquals("Only the default group exists", expectedGroupIDs, actualGroupIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    // GameClient#createGroups
    @Test
    public void createGroups_valid_new_group() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1"));
            Set<String> actualGroupIDs = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups().stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-1"));
            Assert.assertEquals("Groups IDs are what is expected", expectedGroupIDs, actualGroupIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createGroups_valid_new_group_non_default_scene() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1", "scene-1"));
            Set<String> actualGroupIDs = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups().stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-1"));
            Assert.assertEquals("Groups IDs are what is expected", expectedGroupIDs, actualGroupIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createGroups_valid_etag_specified() {
        try {
            String etag = UUID.randomUUID().toString();
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1", etag, "scene-1"));
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Set<String> actualGroupIDs = actualGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-1"));
            Assert.assertEquals("Groups IDs are what is expected", expectedGroupIDs, actualGroupIDs);

            int groupsFound = 0;
            for (InteractiveGroup group : actualGroups) {
                if ("group-1".equals(group.getGroupID())) {
                    groupsFound++;
                    Assert.assertEquals("Group has specified etag", etag, group.getEtag());
                }
            }
            Assert.assertEquals("Only one group matched", 1, groupsFound);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createGroups_valid_new_group_with_meta() {
        try {
            InteractiveGroup group = new InteractiveGroup("group-1").addMetaProperty("awesome_key", "awesome_value");
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(group);
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Set<String> actualGroupIDs = actualGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-1"));
            Assert.assertEquals("Groups IDs are what is expected", expectedGroupIDs, actualGroupIDs);

            int foundGroups = 0;
            for (InteractiveGroup actualGroup : actualGroups) {
                if ("group-1".equals(actualGroup.getGroupID())) {
                    foundGroups++;
                    Assert.assertEquals("Group has the new meta property", "awesome_value", group.getMeta().get("awesome_key").getAsJsonObject().get("value").getAsString());
                }
            }
            Assert.assertEquals("Only the expected group had the new meta property", 1, foundGroups);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createGroups_valid_multiple_new_group() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1"), new InteractiveGroup("group-2"), new InteractiveGroup("group-3"));
            Set<String> actualGroupIDs = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups().stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-1", "group-2", "group-3"));
            Assert.assertEquals("Groups IDs are what is expected", expectedGroupIDs, actualGroupIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createGroups_invalid_cannot_create_default_group() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("default"));
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot create default group", 4009, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createGroups_invalid_cannot_create_duplicate_groups() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1"), new InteractiveGroup("group-1"));
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot create duplicate group", 4009, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createGroups_invalid_create_group_fails_on_one_group() {
        InteractiveGroup groupOne = new InteractiveGroup("group-1").addMetaProperty("awesome_key", "awesome_value");

        InteractiveGroup groupTwo = new InteractiveGroup("group-2");
        JsonObject badMetaObject = new JsonObject();
        badMetaObject.addProperty("bad_key", "bad_value");
        groupTwo.setMeta(badMetaObject);

        // Attempt to create groups where one group will throw an error
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(groupOne, groupTwo);
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot create groups when one group has an error", 4000, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }

        // Verify that no new groups were created
        try {
            Set<String> actualGroupIDs = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups().stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default"));
            Assert.assertEquals("No new groups where created", expectedGroupIDs, actualGroupIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void createGroups_invalid_cannot_create_group_for_nonexistent_scene() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1", "scene-banana"));
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot create group for a nonexistent scene", 4010, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    // GameClient#updateGroups
    @Test
    public void updateGroups_valid_add_meta() {
        try {
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Assert.assertEquals("At least one group exists", 1, actualGroups.size());
            InteractiveGroup group = actualGroups.iterator().next().addMetaProperty("awesome_key", "awesome_value");
            Set<InteractiveGroup> updatedGroups = gameClient.using(GROUP_SERVICE_PROVIDER).updateGroups(group);

            Set<String> updatedGroupIDs = updatedGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList(group.getGroupID()));
            Assert.assertEquals("Only expected groups were updated", expectedGroupIDs, updatedGroupIDs);

            int foundGroups = 0;
            for (InteractiveGroup actualGroup : updatedGroups) {
                if (group.getGroupID().equals(actualGroup.getGroupID())) {
                    foundGroups++;
                    Assert.assertEquals("Group has the new meta property", "awesome_value", group.getMeta().get("awesome_key").getAsJsonObject().get("value").getAsString());
                }
            }
            Assert.assertEquals("Only the expected group had the new meta property", 1, foundGroups);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateGroups_valid_change_to_existing_scene() {
        try {
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Assert.assertEquals("At least one group exists", 1, actualGroups.size());
            InteractiveGroup group = actualGroups.iterator().next().setScene("scene-1");
            Set<InteractiveGroup> updatedGroups = gameClient.using(GROUP_SERVICE_PROVIDER).updateGroups(group);

            Set<String> updatedGroupIDs = updatedGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList(group.getGroupID()));
            Assert.assertEquals("Only expected groups were updated", expectedGroupIDs, updatedGroupIDs);

            int foundGroups = 0;
            for (InteractiveGroup actualGroup : updatedGroups) {
                if (group.getGroupID().equals(actualGroup.getGroupID())) {
                    foundGroups++;
                    Assert.assertEquals("Group has the new meta property", group.getSceneID(), actualGroup.getSceneID());
                }
            }
            Assert.assertEquals("Only the expected group had the new meta property", 1, foundGroups);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateScenes_invalid_change_to_non_existent_scene() {
        try {
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Assert.assertEquals("At least one group exists", 1, actualGroups.size());
            InteractiveGroup group = actualGroups.iterator().next().setScene("scene-banana");
            gameClient.using(GROUP_SERVICE_PROVIDER).updateGroups(group);

            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot update group with a nonexistent scene", 4010, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateScene_invalid_etag() {
        try {
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Assert.assertEquals("At least one group exists", 1, actualGroups.size());
            InteractiveGroup group = actualGroups.iterator().next().setScene("scene-1");
            gameClient.using(GROUP_SERVICE_PROVIDER).updateGroups(group);
            gameClient.using(GROUP_SERVICE_PROVIDER).updateGroups(group);

            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Etag mismatch", 4005, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateScenes_invalid_one_group_fails() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1"), new InteractiveGroup("group-2"));
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Assert.assertEquals("Number of groups matches expectation", 3, actualGroups.size());
            for (InteractiveGroup group : actualGroups) {
                group.setScene("scene-1");
            }
            JsonObject badMeta = new JsonObject();
            badMeta.addProperty("bad_key", "bad_value");
            InteractiveGroup group = actualGroups.iterator().next().setMeta(badMeta);
            gameClient.using(GROUP_SERVICE_PROVIDER).updateGroups(actualGroups);
            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Bad meta property check", 4000, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }

        try {
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Assert.assertEquals("Number of groups matches expectation", 3, actualGroups.size());
            for (InteractiveGroup group : actualGroups) {
                Assert.assertEquals("Scene is default", "default", group.getSceneID());
                Assert.assertEquals("No meta property exists", null, group.getMeta());
            }
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    // GameClient#deleteGroups
    @Test
    public void deleteGroup_valid_reassign_to_default() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1"));
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Set<String> actualGroupIDs = actualGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-1"));
            Assert.assertEquals("Groups matches expectation", expectedGroupIDs, actualGroupIDs);

            gameClient.using(GROUP_SERVICE_PROVIDER).deleteGroup("group-1");

            actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            actualGroupIDs = actualGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            expectedGroupIDs = new HashSet<>(Arrays.asList("default"));
            Assert.assertEquals("Groups matches expectation", expectedGroupIDs, actualGroupIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void deleteGroup_valid_reassign_to_non_default() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1"), new InteractiveGroup("group-2"));
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Set<String> actualGroupIDs = actualGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-1", "group-2"));
            Assert.assertEquals("Groups matches expectation", expectedGroupIDs, actualGroupIDs);

            gameClient.using(GROUP_SERVICE_PROVIDER).deleteGroup("group-1", "group-2");

            actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            actualGroupIDs = actualGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-2"));
            Assert.assertEquals("Groups matches expectation", expectedGroupIDs, actualGroupIDs);
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void deleteGroup_invalid_cannot_delete_default_group() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1"), new InteractiveGroup("group-2"));
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Set<String> actualGroupIDs = actualGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-1", "group-2"));
            Assert.assertEquals("Groups matches expectation", expectedGroupIDs, actualGroupIDs);

            gameClient.using(GROUP_SERVICE_PROVIDER).deleteGroup("default", "group-2");

            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Delete default group", 4018, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void deleteGroup_invalid_cannot_reassign_to_self() {
        try {
            gameClient.using(GROUP_SERVICE_PROVIDER).createGroups(new InteractiveGroup("group-1"), new InteractiveGroup("group-2"));
            Set<InteractiveGroup> actualGroups = gameClient.using(GROUP_SERVICE_PROVIDER).getGroups();
            Set<String> actualGroupIDs = actualGroups.stream().map(InteractiveGroup::getGroupID).collect(Collectors.toSet());
            Set<String> expectedGroupIDs = new HashSet<>(Arrays.asList("default", "group-1", "group-2"));
            Assert.assertEquals("Groups matches expectation", expectedGroupIDs, actualGroupIDs);

            gameClient.using(GROUP_SERVICE_PROVIDER).deleteGroup("group-2", "group-2");

            Assert.fail();
        }
        catch (InteractiveReplyWithErrorException e) {
            Assert.assertEquals("Cannot reassign to self", 4008, e.getError().getErrorCode());
        }
        catch (InteractiveException e) {
            Assert.fail(e.getMessage());
        }
    }
}
