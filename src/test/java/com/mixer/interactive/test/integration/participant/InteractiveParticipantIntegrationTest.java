package com.mixer.interactive.test.integration.participant;

import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.event.participant.ParticipantJoinEvent;
import com.mixer.interactive.event.participant.ParticipantLeaveEvent;
import com.mixer.interactive.event.participant.ParticipantUpdateEvent;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.resources.participant.InteractiveParticipant;
import com.mixer.interactive.test.util.InteractiveTestParticipant;
import com.mixer.interactive.test.util.TestEventHandler;
import com.mixer.interactive.test.util.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.mixer.interactive.GameClient.GROUP_SERVICE_PROVIDER;
import static com.mixer.interactive.GameClient.PARTICIPANT_SERVICE_PROVIDER;
import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests <code>InteractiveParticipant</code> update operations to the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.0.0
 */
public class InteractiveParticipantIntegrationTest {

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
        TestUtils.waitForWebSocket();
        TestEventHandler.HANDLER.clear();
        TestUtils.TEST_PARTICIPANTS.forEach(InteractiveTestParticipant::disconnect);
        gameClient.disconnect();
        gameClient = null;
    }

    @Test
    public void can_get_all_participants() {
        try {
            Set<InteractiveParticipant> participants = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> TEST_PARTICIPANTS.get(0).connect())
                    .thenCompose(connected -> TEST_PARTICIPANTS.get(0).connect())
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(connected -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get();

            Assert.assertEquals("The expected number of participants were returned", 1, participants.size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_get_active_participants() {
        try {
            Set<InteractiveParticipant> participants = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> TEST_PARTICIPANTS.get(0).connect())
                    .thenCompose(connected -> TEST_PARTICIPANTS.get(1).connect())
                    .thenRun(() -> gameClient.ready(true))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenRun(() -> TEST_PARTICIPANTS.get(0).giveInput("d-button-1"))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(connected -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getActiveParticipants(Instant.now().toEpochMilli()-10000))
                    .get();

            Assert.assertEquals("The expected number of participants were returned", 1, participants.size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_update_participant() {
        try {
            InteractiveParticipant participant = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> TestUtils.TEST_PARTICIPANTS.get(0).connect())
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(clientConnected -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get().iterator().next();

            Set<InteractiveParticipant> participants = gameClient.using(PARTICIPANT_SERVICE_PROVIDER).update(participant.addMetaProperty("awesome-property", 4)).get();
            Assert.assertEquals("The participants meta has been updated",
                    true,
                    participants.stream().anyMatch(updatedParticipant -> participant.getSessionID().equals(updatedParticipant.getSessionID()) && ((JsonObject) updatedParticipant.getMeta().get("awesome-property")).get("value").getAsInt() == 4));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_change_group() {
        try {
            InteractiveGroup testGroup = new InteractiveGroup("group-1");
            Set<InteractiveParticipant> participants = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> TestUtils.TEST_PARTICIPANTS.get(0).connect())
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(weWaited -> gameClient.using(GROUP_SERVICE_PROVIDER).create(testGroup))
                    .thenCompose(clientConnected -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get();
            for (InteractiveParticipant participant : participants) {
                Assert.assertEquals("Participants are in the default group initially", "default", participant.getGroupID());
            }

            participants.forEach(participant -> participant.changeGroup(testGroup.getGroupID()));
            participants = gameClient.using(PARTICIPANT_SERVICE_PROVIDER).update(participants).get();
            for (InteractiveParticipant participant : participants) {
                Assert.assertNotEquals("Participant are in the expected group", testGroup, participant.getGroupID());
            }
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_change_group_to_non_existent_group() {
        try {
            Set<InteractiveParticipant> participants = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(aVoid -> TestUtils.TEST_PARTICIPANTS.get(0).connect())
                    .thenCompose(clientConnected -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get();
            for (InteractiveParticipant participant : participants) {
                Assert.assertEquals("Participants are in the default group initially", "default", participant.getGroupID());
            }

            participants.forEach(participant -> participant.changeGroup("banana-group"));
            gameClient.using(PARTICIPANT_SERVICE_PROVIDER).update(participants).get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot move participant to non-existent group", 4008, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void can_enable_and_disable_participants() {
        try {
            Set<InteractiveParticipant> participants = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> TestUtils.TEST_PARTICIPANTS.get(0).connect())
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(clientConnected -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get();
            for (InteractiveParticipant participant : participants) {
                Assert.assertEquals("Participant is enabled initially", false, participant.isDisabled());
            }

            participants.forEach(participant -> participant.setDisabled(true));
            participants = gameClient.using(PARTICIPANT_SERVICE_PROVIDER).update(participants).get();
            for (InteractiveParticipant participant : participants) {
                Assert.assertEquals("Participant is disabled", true, participant.isDisabled());
            }

            participants.forEach(participant -> participant.setDisabled(false));
            participants = gameClient.using(PARTICIPANT_SERVICE_PROVIDER).update(participants).get();
            for (InteractiveParticipant participant : participants) {
                Assert.assertEquals("Participant is disabled", false, participant.isDisabled());
            }
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_self_update() {
        try {
            InteractiveParticipant participant = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> TestUtils.TEST_PARTICIPANTS.get(0).connect())
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(aVoid -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get().iterator().next();
            Assert.assertEquals("Participant was updated", true, participant.addMetaProperty("awesome-property", 4).update(gameClient).get());

            Set<InteractiveParticipant> participants = gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants().get();
            Assert.assertEquals("Participant has the new value",
                    true,
                    participants.stream().anyMatch(interactiveParticipant -> interactiveParticipant.getSessionID().equals(participant.getSessionID())
                            && interactiveParticipant.getMeta() != null
                            && ((JsonObject) interactiveParticipant.getMeta().get("awesome-property")).get("value").getAsInt() == 4));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void participant_join_event_posted() {
        try {
            Set<String> participantSessionIds = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> TestUtils.TEST_PARTICIPANTS.get(0).connect())
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(aVoid -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get()
                    .stream()
                    .map(InteractiveParticipant::getSessionID)
                    .collect(Collectors.toSet());

            Assert.assertEquals("A participant join event was posted for the expected participant",
                    true,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .anyMatch(event -> {
                                if (event instanceof ParticipantJoinEvent) {
                                    for (InteractiveParticipant participant : ((ParticipantJoinEvent) event).getParticipants()) {
                                        if (participantSessionIds.contains(participant.getSessionID())) {
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            }));
            Assert.assertEquals("Only one participant join event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> {
                                if (event instanceof ParticipantJoinEvent) {
                                    for (InteractiveParticipant participant : ((ParticipantJoinEvent) event).getParticipants()) {
                                        if (participantSessionIds.contains(participant.getSessionID())) {
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            })
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void participant_updated_event_posted() {
        try {
            Set<InteractiveParticipant> participants = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> TestUtils.TEST_PARTICIPANTS.get(0).connect())
                    .thenRun(TestUtils::waitForWebSocket)
                    .thenCompose(aVoid -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get();

            gameClient.using(GROUP_SERVICE_PROVIDER).create(new InteractiveGroup("group-1")).get();

            participants.forEach(participant -> participant.changeGroup("group-1"));
            Set<String> participantSessionIds = gameClient.using(PARTICIPANT_SERVICE_PROVIDER).update(participants).get()
                    .stream().map(InteractiveParticipant::getSessionID).collect(Collectors.toSet());

            Assert.assertEquals("A participant update event was posted for the expected participant",
                    true,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .anyMatch(event -> {
                                if (event instanceof ParticipantUpdateEvent) {
                                    for (InteractiveParticipant participant : ((ParticipantUpdateEvent) event).getParticipants()) {
                                        if (participantSessionIds.contains(participant.getSessionID())) {
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            }));
            Assert.assertEquals("Only one participant update event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> {
                                if (event instanceof ParticipantUpdateEvent) {
                                    for (InteractiveParticipant participant : ((ParticipantUpdateEvent) event).getParticipants()) {
                                        if (participantSessionIds.contains(participant.getSessionID())) {
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            })
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void participant_leave_event_posted() {
        try {
            InteractiveTestParticipant testParticipant = TestUtils.TEST_PARTICIPANTS.get(0);
            Set<String> participantSessionIds = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> testParticipant.connect())
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(aVoid -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get()
                    .stream()
                    .map(InteractiveParticipant::getSessionID)
                    .collect(Collectors.toSet());

            CompletableFuture.runAsync(testParticipant::disconnect)
                    .thenRun(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("A participant leave event was posted for the expected participant",
                    true,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .anyMatch(event -> {
                                if (event instanceof ParticipantLeaveEvent) {
                                    for (InteractiveParticipant participant : ((ParticipantLeaveEvent) event).getParticipants()) {
                                        if (participantSessionIds.contains(participant.getSessionID())) {
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            }));
            Assert.assertEquals("Only one participant leave event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> {
                                if (event instanceof ParticipantLeaveEvent) {
                                    for (InteractiveParticipant participant : ((ParticipantLeaveEvent) event).getParticipants()) {
                                        if (participantSessionIds.contains(participant.getSessionID())) {
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            })
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }
}
