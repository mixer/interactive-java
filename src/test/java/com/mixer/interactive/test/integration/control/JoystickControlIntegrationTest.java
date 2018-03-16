package com.mixer.interactive.test.integration.control;

import com.mixer.interactive.GameClient;
import com.mixer.interactive.resources.control.InteractiveCanvasSize;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.control.InteractiveControlPosition;
import com.mixer.interactive.resources.control.JoystickControl;
import com.mixer.interactive.test.util.TestEventHandler;
import com.mixer.interactive.test.util.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.mixer.interactive.GameClient.CONTROL_SERVICE_PROVIDER;
import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests <code>JoystickControl</code> create/update/delete operations to the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.0.0
 */
public class JoystickControlIntegrationTest {

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
    public void can_self_create() {
        try {
            JoystickControl control = new JoystickControl("test-control").addPosition(new InteractiveControlPosition(InteractiveCanvasSize.LARGE));
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            Assert.assertEquals("Joystick control was created", true, control.create(gameClient).get());

            boolean controlExists = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(control::equals)))
                    .get();
            Assert.assertEquals("Control exists", true, controlExists);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_self_update() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            JoystickControl control = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof JoystickControl) {
                                return CompletableFuture.completedFuture((JoystickControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("Unable to find a joystick control to test");
            }
            Assert.assertEquals("Joystick control was updated", true, control.setSampleRate(90).update(gameClient).get());

            boolean controlExists = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(updatedControl -> updatedControl instanceof JoystickControl
                            && updatedControl.getControlID().equals(control.getControlID())
                            && ((JoystickControl) updatedControl).getSampleRate().equals(control.getSampleRate()))))
                    .get();
            Assert.assertEquals("Control exists", true, controlExists);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_self_delete() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            JoystickControl control = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof JoystickControl) {
                                return CompletableFuture.completedFuture((JoystickControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("Unable to find a joystick control to test");
            }
            Assert.assertEquals("Joystick control was deleted", true, control.delete(gameClient).get());

            boolean controlExists = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(control::equals)))
                    .get();
            Assert.assertEquals("Control does not exist", false, controlExists);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }
}
