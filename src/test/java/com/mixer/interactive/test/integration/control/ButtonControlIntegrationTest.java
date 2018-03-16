package com.mixer.interactive.test.integration.control;

import com.mixer.interactive.GameClient;
import com.mixer.interactive.resources.control.ButtonControl;
import com.mixer.interactive.resources.control.InteractiveCanvasSize;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.control.InteractiveControlPosition;
import com.mixer.interactive.test.util.TestEventHandler;
import com.mixer.interactive.test.util.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.mixer.interactive.GameClient.CONTROL_SERVICE_PROVIDER;
import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests <code>ButtonControl</code> create/update/delete operations to the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.0.0
 */
public class ButtonControlIntegrationTest {

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
    public void can_set_cost() {
        try {
            ButtonControl control = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(created -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof ButtonControl) {
                                return CompletableFuture.completedFuture((ButtonControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("No button control was found to test");
            }

            Set<InteractiveControl> updatedControls = gameClient.using(CONTROL_SERVICE_PROVIDER).update(control.setCost(90))
                    .thenCompose(updatePromises -> {
                        for (InteractiveControl key : updatePromises.keySet()) {
                            if (key.getControlID().equals(control.getControlID())) {
                                return updatePromises.get(key);
                            }
                        }
                        return CompletableFuture.completedFuture(Collections.emptySet());
                    })
                    .get();

            Assert.assertEquals("The control was updated", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID())));
            Assert.assertEquals("The control has the specified cost value", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID()) && Objects.equals(((ButtonControl) updatedControl).getCost(), control.getCost())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_set_keyCode() {
        try {
            ButtonControl control = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(created -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof ButtonControl) {
                                return CompletableFuture.completedFuture((ButtonControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("No button control was found to test");
            }

            Set<InteractiveControl> updatedControls = gameClient.using(CONTROL_SERVICE_PROVIDER).update(control.setKeyCode(90))
                    .thenCompose(updatePromises -> {
                        for (InteractiveControl key : updatePromises.keySet()) {
                            if (key.getControlID().equals(control.getControlID())) {
                                return updatePromises.get(key);
                            }
                        }
                        return CompletableFuture.completedFuture(Collections.emptySet());
                    })
                    .get();

            Assert.assertEquals("The control was updated", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID())));
            Assert.assertEquals("The control has the specified keyCode value", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID()) && Objects.equals(((ButtonControl) updatedControl).getKeyCode(), control.getKeyCode())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_set_tooltip() {
        try {
            ButtonControl control = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(created -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof ButtonControl) {
                                return CompletableFuture.completedFuture((ButtonControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("No button control was found to test");
            }

            Set<InteractiveControl> updatedControls = gameClient.using(CONTROL_SERVICE_PROVIDER).update(control.setTooltip("malm"))
                    .thenCompose(updatePromises -> {
                        for (InteractiveControl key : updatePromises.keySet()) {
                            if (key.getControlID().equals(control.getControlID())) {
                                return updatePromises.get(key);
                            }
                        }
                        return CompletableFuture.completedFuture(Collections.emptySet());
                    })
                    .get();

            Assert.assertEquals("The control was updated", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID())));
            Assert.assertEquals("The control has the specified tooltip value", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID()) && Objects.equals(((ButtonControl) updatedControl).getTooltip(), control.getTooltip())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_set_progress() {
        try {
            ButtonControl control = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(created -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof ButtonControl) {
                                return CompletableFuture.completedFuture((ButtonControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("No button control was found to test");
            }

            Set<InteractiveControl> updatedControls = gameClient.using(CONTROL_SERVICE_PROVIDER).update(control.setProgress(0.5F))
                    .thenCompose(updatePromises -> {
                        for (InteractiveControl key : updatePromises.keySet()) {
                            if (key.getControlID().equals(control.getControlID())) {
                                return updatePromises.get(key);
                            }
                        }
                        return CompletableFuture.completedFuture(Collections.emptySet());
                    })
                    .get();

            Assert.assertEquals("The control was updated", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID())));
            Assert.assertEquals("The control has the specified progress value", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID()) && Objects.equals(((ButtonControl) updatedControl).getProgress(), control.getProgress())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_set_text() {
        try {
            ButtonControl control = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(created -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof ButtonControl) {
                                return CompletableFuture.completedFuture((ButtonControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("No button control was found to test");
            }

            Set<InteractiveControl> updatedControls = gameClient.using(CONTROL_SERVICE_PROVIDER).update(control.setText("mappa"))
                    .thenCompose(updatePromises -> {
                        for (InteractiveControl key : updatePromises.keySet()) {
                            if (key.getControlID().equals(control.getControlID())) {
                                return updatePromises.get(key);
                            }
                        }
                        return CompletableFuture.completedFuture(Collections.emptySet());
                    })
                    .get();

            Assert.assertEquals("The control was updated", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID())));
            Assert.assertEquals("The control has the specified text value", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID()) && Objects.equals(((ButtonControl) updatedControl).getText(), control.getText())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_set_cooldown() {
        try {
            ButtonControl control = gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenCompose(created -> gameClient.using(CONTROL_SERVICE_PROVIDER).getControls())
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof ButtonControl) {
                                return CompletableFuture.completedFuture((ButtonControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("No button control was found to test");
            }

            Set<InteractiveControl> updatedControls = gameClient.using(CONTROL_SERVICE_PROVIDER).update(control.setCooldown(Instant.now().plusSeconds(120)))
                    .thenCompose(updatePromises -> {
                        for (InteractiveControl key : updatePromises.keySet()) {
                            if (key.getControlID().equals(control.getControlID())) {
                                return updatePromises.get(key);
                            }
                        }
                        return CompletableFuture.completedFuture(Collections.emptySet());
                    })
                    .get();

            Assert.assertEquals("The control was updated", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID())));
            Assert.assertEquals("The control has the specified cooldown value", true, updatedControls.stream().anyMatch(updatedControl -> updatedControl.getControlID().equals(control.getControlID()) && Objects.equals(((ButtonControl) updatedControl).getCooldown(), control.getCooldown())));
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void can_self_create() {
        try {
            ButtonControl control = new ButtonControl("test-control-7").addPosition(new InteractiveControlPosition(InteractiveCanvasSize.LARGE));
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            Assert.assertEquals("Button control was created", true, control.create(gameClient).get());

            boolean controlExists = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(control::equals)))
                    .get();
            Assert.assertEquals("Button exists", true, controlExists);
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void can_self_update() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get();
            ButtonControl control = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof ButtonControl) {
                                return CompletableFuture.completedFuture((ButtonControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("Unable to find a button control to test");
            }
            Assert.assertEquals("Button control was updated", true, control.setCost(90).setTooltip("What a cool button!").update(gameClient).get());

            boolean controlExists = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()
                    .thenCompose(controls -> CompletableFuture.supplyAsync(() -> controls.stream().anyMatch(updatedControl -> updatedControl instanceof ButtonControl
                            && updatedControl.getControlID().equals(control.getControlID())
                            && ((ButtonControl) updatedControl).getCost().equals(control.getCost())
                            && ((ButtonControl) updatedControl).getTooltip().equals("What a cool button!"))))
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
            ButtonControl control = gameClient.using(CONTROL_SERVICE_PROVIDER).getControls()
                    .thenCompose(interactiveControls -> {
                        for (InteractiveControl interactiveControl : interactiveControls) {
                            if (interactiveControl instanceof ButtonControl) {
                                return CompletableFuture.completedFuture((ButtonControl) interactiveControl);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    })
                    .get();

            if (control == null) {
                Assert.fail("Unable to find a button control to test");
            }
            Assert.assertEquals("Button control was deleted", true, control.delete(gameClient).get());

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
