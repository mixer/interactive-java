package com.mixer.interactive.test.integration.participant;

import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.event.participant.ParticipantJoinEvent;
import com.mixer.interactive.event.participant.ParticipantLeaveEvent;
import com.mixer.interactive.event.participant.ParticipantUpdateEvent;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.resources.participant.InteractiveParticipant;
import com.mixer.interactive.test.util.MockParticipantClient;
import com.mixer.interactive.test.util.TestEventHandler;
import com.mixer.interactive.test.util.TestUtils;
import org.junit.*;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.mixer.interactive.GameClient.GROUP_SERVICE_PROVIDER;
import static com.mixer.interactive.GameClient.PARTICIPANT_SERVICE_PROVIDER;
import static com.mixer.interactive.test.util.TestUtils.*;
import static org.junit.Assume.assumeTrue;

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

    @BeforeClass
    public static void isLocal() {
        assumeTrue(INTERACTIVE_SERVICE_URI.getHost().equals("localhost") || INTERACTIVE_SERVICE_URI.getHost().equals("127.0.0.1"));
    }

    @Before
    public void setupGameClient() {
        gameClient = new GameClient(INTERACTIVE_PROJECT_ID);
        gameClient.getEventBus().register(TestEventHandler.HANDLER);
    }

    @After
    public void teardownGameClient() {
        TestEventHandler.HANDLER.clear();
        gameClient.disconnect();
        gameClient = null;
    }

    @Test
    public void can_get_all_participants() {
        try {
            MockParticipantClient mockParticipantClient = new MockParticipantClient();
            Set<InteractiveParticipant> participants = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(mockParticipantClient.connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(aVoid -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get();
            Assert.assertEquals("The expected number of participants were returned", 1, participants.size());

            participants = CompletableFuture.runAsync(() -> {
                        try {
                            mockParticipantClient.closeBlocking();
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    })
                    .thenCompose(aVoid -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants()).get();
            Assert.assertEquals("The expected number of participants were returned", 0, participants.size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_get_active_participants() {
        try {
            MockParticipantClient mockParticipantClient = new MockParticipantClient();
            Set<InteractiveParticipant> participants = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(mockParticipantClient.connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
                    .thenRun(() -> gameClient.ready(true))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenRun(() -> mockParticipantClient.giveInput("d-button-1", "mousedown"))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(aVoid -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getActiveParticipants(0))
                    .get();
            Assert.assertEquals("The expected number of participants were returned", 1, participants.size());

            participants = CompletableFuture.runAsync(() -> {
                        try {
                            mockParticipantClient.closeBlocking();
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    })
                    .thenCompose(aVoid -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants()).get();
            Assert.assertEquals("The expected number of participants were returned", 0, participants.size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_update_participant() {
        try {
            InteractiveParticipant participant = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(new MockParticipantClient().connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
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
            Set<InteractiveParticipant> participants = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(new MockParticipantClient().connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
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
            Set<InteractiveParticipant> participants = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(new MockParticipantClient().connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
                    .thenRunAsync(TestUtils::waitForWebSocket)
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
            Set<InteractiveParticipant> participants = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(new MockParticipantClient().connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
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
            InteractiveParticipant participant = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(new MockParticipantClient().connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
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
            Set<String> participantSessionIds = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(new MockParticipantClient().connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
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
            Set<InteractiveParticipant> participants = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(new MockParticipantClient().connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
                    .thenRunAsync(TestUtils::waitForWebSocket)
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
            MockParticipantClient mockParticipantClient = new MockParticipantClient();
            Set<String> participantSessionIds = gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> {
                        try {
                            return CompletableFuture.completedFuture(mockParticipantClient.connectBlocking());
                        }
                        catch (InterruptedException e) {
                            return CompletableFuture.completedFuture(false);
                        }
                    })
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .thenCompose(aVoid -> gameClient.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants())
                    .get()
                    .stream()
                    .map(InteractiveParticipant::getSessionID)
                    .collect(Collectors.toSet());

            CompletableFuture.runAsync(() -> {
                try {
                    mockParticipantClient.closeBlocking();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).thenRunAsync(TestUtils::waitForWebSocket).get();

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
