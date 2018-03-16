package com.mixer.interactive.test.integration.control;

import com.mixer.interactive.GameClient;
import com.mixer.interactive.event.control.ControlCreateEvent;
import com.mixer.interactive.event.control.ControlDeleteEvent;
import com.mixer.interactive.event.control.ControlUpdateEvent;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.resources.control.ButtonControl;
import com.mixer.interactive.resources.control.InteractiveCanvasSize;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.control.InteractiveControlPosition;
import com.mixer.interactive.test.util.TestEventHandler;
import com.mixer.interactive.test.util.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.mixer.interactive.GameClient.CONTROL_SERVICE_PROVIDER;
import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests <code>InteractiveControl</code> create/update/delete operations to the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.0.0
 */
public class InteractiveControlIntegrationTest {

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
    public void teardownGameClient() throws InterruptedException {
        TestEventHandler.HANDLER.clear();
        gameClient.disconnect();
        gameClient = null;
        Thread.sleep(500);
    }

    @Test
    public void can_get_controls() {
        try {
            Set<String> expectedControlIds = new HashSet<>(Arrays.asList("d-button-1", "d-button-2", "d-button-3", "d-button-4", "d-joystick-1", "d-joystick-2",
                    "1-button-1", "1-button-2", "1-button-3", "1-button-4", "1-joystick-1", "1-joystick-2",
                    "2-button-1", "2-button-2", "2-button-3", "2-button-4", "2-joystick-1", "2-joystick-2"));
            Set<InteractiveControl> controls = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .get();

            Assert.assertEquals("The expected controls are present", expectedControlIds, controls.stream().map(InteractiveControl::getControlID).collect(Collectors.toSet()));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_create_control() {
        try {
            ButtonControl buttonControl = new ButtonControl("test-button");
            Boolean created = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).create(buttonControl))
                    .thenCompose(createPromises -> createPromises.containsKey(buttonControl)
                            ? createPromises.get(buttonControl)
                            : CompletableFuture.completedFuture(false))
                    .get();
            Assert.assertEquals("The control was created", true, created);

            Set<InteractiveControl> controls = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls().get();
            Assert.assertEquals("The control exists", true, controls.stream().anyMatch(control -> buttonControl.getControlID().equals(control.getControlID())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_create_duplicate_control() {
        try {
            ButtonControl firstControl = new ButtonControl("first-button");
            ButtonControl secondControl = new ButtonControl("first-button");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).create(firstControl, secondControl))
                    .get();
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot create a duplicate control", 4013, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_create_control_in_non_existent_scene() {
        try {
            InteractiveControl control = new ButtonControl("test-button", "awesome-scene");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).create(control))
                    .thenCompose(createPromises -> createPromises.get(control))
                    .get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot create a control in a non-existent scene", 4010, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void can_update_control_and_add_new_position() {
        try {
            Set<InteractiveControl> controls = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .get();
            InteractiveControl control = controls.iterator().next();
            InteractiveControlPosition position = new InteractiveControlPosition(InteractiveCanvasSize.MEDIUM);
            control.setPositions(position);
            Map<InteractiveControl, CompletableFuture<Set<InteractiveControl>>> updatedControls = gameClient.using(CONTROL_SERVICE_PROVIDER).update(control).get();
            Assert.assertEquals("Only one control was updated", 1, updatedControls.size());
            Assert.assertEquals("Only the expected control was updated", Collections.singletonList(control.getControlID()), updatedControls.get(control).get().stream().map(InteractiveControl::getControlID).collect(Collectors.toList()));

            InteractiveControl updatedControl = updatedControls.get(control).get().iterator().next();
            Assert.assertEquals("The new X position is what we expect", position.getX(), updatedControl.getPositionFor(InteractiveCanvasSize.MEDIUM).getX());
            Assert.assertEquals("The new Y position is what we expect", position.getY(), updatedControl.getPositionFor(InteractiveCanvasSize.MEDIUM).getY());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_update_control_and_change_its_position() {
        try {
            Set<InteractiveControl> controls = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .get();
            InteractiveControl control = controls.iterator().next();
            InteractiveControlPosition oldPosition = control.getPositionFor(InteractiveCanvasSize.LARGE);
            InteractiveControlPosition newPosition = new InteractiveControlPosition(InteractiveCanvasSize.LARGE, oldPosition.getWidth(), oldPosition.getHeight(), 1, 1);
            control.setPositions(newPosition);
            Map<InteractiveControl, CompletableFuture<Set<InteractiveControl>>> updatedControls = gameClient.using(CONTROL_SERVICE_PROVIDER).update(control).get();
            Assert.assertEquals("Only one control was updated", 1, updatedControls.size());
            Assert.assertEquals("Only the expected control was updated", Collections.singletonList(control.getControlID()), updatedControls.get(control).get().stream().map(InteractiveControl::getControlID).collect(Collectors.toList()));

            InteractiveControl updatedControl = updatedControls.get(control).get().iterator().next();
            Assert.assertNotEquals("The X position of the control has changed", oldPosition.getX(), updatedControl.getPositionFor(InteractiveCanvasSize.LARGE).getX());
            Assert.assertNotEquals("The Y position of the control has changed", oldPosition.getY(), updatedControl.getPositionFor(InteractiveCanvasSize.LARGE).getY());
            Assert.assertEquals("The new X position is what we expect", newPosition.getX(), updatedControl.getPositionFor(InteractiveCanvasSize.LARGE).getX());
            Assert.assertEquals("The new Y position is what we expect", newPosition.getY(), updatedControl.getPositionFor(InteractiveCanvasSize.LARGE).getY());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_update_control_and_remove_a_position() {
        try {
            Set<InteractiveControl> controls = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .get()
                    .stream()
                    .filter(interactiveControl -> interactiveControl.hasPositionFor(InteractiveCanvasSize.MEDIUM))
                    .collect(Collectors.toSet());
            InteractiveControl control = controls.iterator().next();
            control.removePosition(InteractiveCanvasSize.MEDIUM);

            Map<InteractiveControl, CompletableFuture<Set<InteractiveControl>>> updatedControls = gameClient.using(CONTROL_SERVICE_PROVIDER).update(control).get();
            Assert.assertEquals("Only one control was updated", 1, updatedControls.size());
            Assert.assertEquals("Only the expected control was updated", Collections.singletonList(control.getControlID()), updatedControls.get(control).get().stream().map(InteractiveControl::getControlID).collect(Collectors.toList()));

            InteractiveControl updatedControl = updatedControls.get(control).get().iterator().next();
            Assert.assertEquals("The removed position no longer exists", null, updatedControl.getPositionFor(InteractiveCanvasSize.MEDIUM));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_delete_control() {
        try {
            InteractiveControl originalControl = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .get().iterator().next();

            Assert.assertEquals("Control was deleted", true, gameClient.using(CONTROL_SERVICE_PROVIDER).delete(originalControl).get().get(originalControl).get());

            Set<InteractiveControl> controls = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls().get();
            Assert.assertEquals("Control no longer exists", false, controls.stream().anyMatch(control -> originalControl.getControlID().equals(control.getControlID())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_delete_control_that_does_not_exist() {
        try {
            ButtonControl control = new ButtonControl("awesome-control");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).delete(control))
                    .get().get(control).get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveReplyWithErrorException) {
                Assert.assertEquals("Cannot delete a control that does not exist", 4012, ((InteractiveReplyWithErrorException) e.getCause()).getError().getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void create_control_event_posted() {
        try {
            ButtonControl control = new ButtonControl("bacon-button");
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).create(control))
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("A create control event was posted", true, TestEventHandler.HANDLER.getEvents().stream()
                    .anyMatch(event -> event instanceof ControlCreateEvent && ((ControlCreateEvent) event).getControls().stream().anyMatch(eventControl -> control.getControlID().equals(eventControl.getControlID()))));
            Assert.assertEquals("Only one create control event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> event instanceof ControlCreateEvent && ((ControlCreateEvent) event).getControls().stream().anyMatch(eventControl -> control.getControlID().equals(eventControl.getControlID())))
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void update_control_event_posted() {
        try {
            InteractiveControl control = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()).get().iterator().next();

            control.addMetaProperty("awesome_property", "awesome_value");
            gameClient.using(CONTROL_SERVICE_PROVIDER).update(control)
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("An update control event was posted for the expected scene", true,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .anyMatch(event -> event instanceof ControlUpdateEvent && ((ControlUpdateEvent) event).getControls().stream().anyMatch(eventControl -> control.getControlID().equals(eventControl.getControlID()))));
            Assert.assertEquals("Only one update control event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> event instanceof ControlUpdateEvent && ((ControlUpdateEvent) event).getControls().stream().anyMatch(eventControl -> control.getControlID().equals(eventControl.getControlID())))
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void delete_control_event_posted() {
        try {
            InteractiveControl control = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(connected -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .thenCompose(controls -> CompletableFuture.completedFuture(controls.iterator().next()))
                    .get();
            LogManager.getLogger().info(control.getControlID());
            gameClient.using(CONTROL_SERVICE_PROVIDER).delete(control)
                    .thenRunAsync(TestUtils::waitForWebSocket)
                    .get();

            Assert.assertEquals("A delete control event was posted for the expected scene", true,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .anyMatch(event -> event instanceof ControlDeleteEvent && ((ControlDeleteEvent) event).getControlIds().stream().anyMatch(deletedControlId -> control.getControlID().equals(deletedControlId))));
            Assert.assertEquals("Only one delete control event was posted for the operation", 1,
                    TestEventHandler.HANDLER.getEvents().stream()
                            .filter(event -> event instanceof ControlDeleteEvent && ((ControlDeleteEvent) event).getControlIds().stream().anyMatch(deletedControlId -> control.getControlID().equals(deletedControlId)))
                            .collect(Collectors.toSet()).size());
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
