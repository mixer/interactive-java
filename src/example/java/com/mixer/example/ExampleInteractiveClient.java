package com.mixer.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.control.ButtonControl;
import com.mixer.interactive.resources.control.InteractiveCanvasSize;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.control.InteractiveControlPosition;
import com.mixer.interactive.resources.core.BandwidthThrottle;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.resources.participant.InteractiveParticipant;
import com.mixer.interactive.resources.scene.InteractiveScene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.*;

import static com.mixer.interactive.GameClient.*;

/**
 * A simple Interactive client example. Connects to an Interactive integration specified in the
 * <code>interactive-project.json</code> properties file. Commands are run via console input.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ExampleInteractiveClient {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Default project version id to be used if one cannot be retrieved from the properties file
     */
    private static final int DEFAULT_PROJECT_VERSION_ID = 1234;

    /**
     * Default OAuth token to be used if one cannot be retrieved from the properties file
     */
    private static final String DEFAULT_OAUTH_TOKEN = "foo";

    /**
     * Project version id read from properties file
     */
    private static int projectVersionID;

    /**
     * OAuth token read from properties file
     */
    private static String oauthToken;

    /**
     * Game client for the project
     */
    public static final GameClient GAME_CLIENT;

    /**
     * Initializes the game client from the properties file.
     *
     * @since   1.0.0
     */
    static {
        File propertyFile;
        URL propertyFileURL = ClassLoader.getSystemClassLoader().getResource("interactive-project.json");

        if (propertyFileURL != null) {
            propertyFile = new File(propertyFileURL.getFile());

            JsonElement jsonElement = null;
            try (JsonReader jsonReader = new JsonReader(new FileReader(propertyFile))) {
                jsonElement = new JsonParser().parse(jsonReader);
            }
            catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }

            if (jsonElement != null) {
                projectVersionID = ((JsonObject) jsonElement).get("projectVersion").getAsInt();
                oauthToken = ((JsonObject) jsonElement).get("oauthToken").getAsString();
            }
            else {
                projectVersionID = DEFAULT_PROJECT_VERSION_ID;
                oauthToken = DEFAULT_OAUTH_TOKEN;
            }
        }
        else {
            projectVersionID = DEFAULT_PROJECT_VERSION_ID;
            oauthToken = DEFAULT_OAUTH_TOKEN;
        }

        GAME_CLIENT = new GameClient(projectVersionID);
    }

    /**
     * Starts the application.
     *
     * @since   1.0.0
     */
    public static void main(String[] args) {
        runGameClient();
    }

    /**
     * Runs the application loop. Exits when <code>stop</code> or <code>quit</code> is entered by the user.
     *
     * @since   1.0.0
     */
    private static void runGameClient() {
        System.out.println("Ready for control");
        String command = "";
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            List<String> commandArguments = new ArrayList<>();
            while (!"stop".equals(command) && !"quit".equals(command)) {
                StringTokenizer commandTokenizer = new StringTokenizer(bufferedReader.readLine());
                commandArguments.clear();
                while (commandTokenizer.hasMoreTokens()) {
                    commandArguments.add(commandTokenizer.nextToken());
                }

                try {
                    if (!commandArguments.isEmpty()) {
                        command = commandArguments.get(0);
                        switch (command) {
                            case "connect": {
                                LOG.info("Connecting");
                                GAME_CLIENT.connect(oauthToken);
                                break;
                            }
                            case "reconnect": {
                                GAME_CLIENT.disconnect();
                                GAME_CLIENT.connect(oauthToken);
                                break;
                            }
                            case "disconnect": {
                                GAME_CLIENT.disconnect();
                                LOG.info("Disconnected");
                                break;
                            }
                            case "ready": {
                                GAME_CLIENT.ready(true);
                                break;
                            }
                            case "notReady": {
                                GAME_CLIENT.ready(false);
                                break;
                            }
                            case "setCompression": {
                                if (commandArguments.size() > 1) {
                                    GAME_CLIENT.setCompression(commandArguments.subList(1, commandArguments.size()).toArray(new String[0]));
                                }
                                else {
                                    LOG.error("Usage: setCompression <scheme1> <scheme2> ... <schemeN>");
                                }
                                break;
                            }
                            case "getTime": {
                                LOG.debug(GAME_CLIENT.getTime().toString());
                                break;
                            }
                            case "getMemoryStats": {
                                LOG.debug(GAME_CLIENT.getMemoryStats().toString());
                                break;
                            }
                            case "getThrottleState": {
                                GAME_CLIENT.getThrottleState().forEach((key, value) -> LOG.info(String.format("Method[%s], %s", key, value)));
                                break;
                            }
                            case "setBandwidthThrottle": {
                                if (commandArguments.size() >= 4) {
                                    Map<InteractiveMethod, BandwidthThrottle> throttleMap = new EnumMap<>(InteractiveMethod.class);
                                    InteractiveMethod method = InteractiveMethod.from(commandArguments.get(1));
                                    int capacity = Integer.parseInt(commandArguments.get(2));
                                    int drainRate = Integer.parseInt(commandArguments.get(3));
                                    if (method != null) {
                                        throttleMap.put(method, new BandwidthThrottle(capacity, drainRate));
                                        GAME_CLIENT.setBandwidthThrottle(throttleMap);
                                    }
                                } else {
                                    LOG.error("Usage: setBandwidthThrottle <method> <capacity> <drainRate>");
                                }
                                break;
                            }
                            case "getAllParticipants": {
                                GAME_CLIENT.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants().forEach(participant -> LOG.info(participant.toString()));
                                break;
                            }
                            case "updateParticipants": {
                                if (commandArguments.size() == 2) {
                                    Set<InteractiveParticipant> participants = GAME_CLIENT.using(PARTICIPANT_SERVICE_PROVIDER).getAllParticipants();
                                    participants.forEach(participant -> participant.changeGroup(commandArguments.get(1)));
                                    GAME_CLIENT.using(PARTICIPANT_SERVICE_PROVIDER).updateParticipants(participants).forEach(participant -> LOG.info(participant.toString()));
                                }
                                else {
                                    LOG.error("Usage: updateParticipants <newGroupName>");
                                }
                                break;
                            }
                            case "getGroups": {
                                GAME_CLIENT.using(GROUP_SERVICE_PROVIDER).getGroups().forEach(participant -> LOG.info(participant.toString()));
                                break;
                            }
                            case "createGroups": {
                                if (commandArguments.size() >= 2) {
                                    Set<InteractiveGroup> groups = new HashSet<>();
                                    for (String groupID : commandArguments.subList(1, commandArguments.size())) {
                                        groups.add(new InteractiveGroup(groupID));
                                    }
                                    GAME_CLIENT.using(GROUP_SERVICE_PROVIDER).createGroups(groups);
                                }
                                else {
                                    LOG.error("Usage: createGroups <groupID_1> <groupID_2> ... <groupID_N>");
                                }
                                break;
                            }
                            case "updateGroup": {
                                if (commandArguments.size() == 3) {
                                    Set<InteractiveGroup> groups = GAME_CLIENT.using(GROUP_SERVICE_PROVIDER).getGroups();
                                    for (InteractiveGroup group : groups) {
                                        if (group.getGroupID().equals(commandArguments.get(1))) {
                                            group.setScene(commandArguments.get(2));
                                            GAME_CLIENT.using(GROUP_SERVICE_PROVIDER).updateGroups(group);
                                            break;
                                        }
                                    }
                                }
                                else {
                                    LOG.error("Usage: updateGroup <groupID> <newSceneID>");
                                }

                                break;
                            }
                            case "deleteGroup": {
                                if (commandArguments.size() == 2) {
                                    GAME_CLIENT.using(GROUP_SERVICE_PROVIDER).deleteGroup(commandArguments.get(1));
                                }
                                else if (commandArguments.size() == 3) {
                                    GAME_CLIENT.using(GROUP_SERVICE_PROVIDER).deleteGroup(commandArguments.get(1), commandArguments.get(2));
                                }
                                else {
                                    LOG.error("Usage: deleteGroup <groupID> (newGroupID)");
                                }

                                break;
                            }
                            case "getScenes": {
                                GAME_CLIENT.using(SCENE_SERVICE_PROVIDER).getScenes().forEach(scene -> LOG.info(scene.toString()));
                                break;
                            }
                            case "createScenes": {
                                if (commandArguments.size() >= 2) {
                                    Set<InteractiveScene> scenes = new HashSet<>();
                                    for (String sceneID : commandArguments.subList(1, commandArguments.size())) {
                                        InteractiveScene scene = new InteractiveScene(sceneID);
                                        scene.supplyControl(() -> new ButtonControl(sceneID)
                                                .setText(sceneID)
                                                .setPositions(new InteractiveControlPosition(InteractiveCanvasSize.LARGE)
                                                        .resize(10,10)
                                                        .moveTo(10, 10)));
                                        scenes.add(scene);
                                    }
                                    GAME_CLIENT.using(SCENE_SERVICE_PROVIDER).createScenes(scenes);
                                }
                                else {
                                    LOG.error("Usage: createScenes <sceneID_1> <sceneID_2> ... <sceneID_N>");
                                }
                                break;
                            }
                            case "updateScenes": {
                                if (commandArguments.size() >= 2) {
                                    Set<InteractiveScene> scenes = GAME_CLIENT.using(SCENE_SERVICE_PROVIDER).getScenes();
                                    for (InteractiveScene scene : scenes) {
                                        for (String sceneID : commandArguments.subList(1, commandArguments.size())) {
                                            if (scene.getSceneID().equals(sceneID)) {
                                                for (InteractiveControl control : scene.getControls()) {
                                                    if (control instanceof ButtonControl) {
                                                        ((ButtonControl) control).setCost(new Random().nextInt(10));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    GAME_CLIENT.using(SCENE_SERVICE_PROVIDER).updateScenes(scenes);
                                }
                                else {
                                    LOG.error("Usage: updateScene <sceneID>");
                                }
                                break;
                            }
                            case "deleteScene": {
                                if (commandArguments.size() == 2) {
                                    GAME_CLIENT.using(SCENE_SERVICE_PROVIDER).deleteScene(commandArguments.get(1));
                                }
                                else if (commandArguments.size() == 3) {
                                    GAME_CLIENT.using(SCENE_SERVICE_PROVIDER).deleteScene(commandArguments.get(1), commandArguments.get(2));
                                }
                                else {
                                    LOG.error("Usage: deleteScene <sceneID> (newSceneID)");
                                }
                                break;
                            }
                            case "stop": {
                                LOG.info("Stopping");
                                break;
                            }
                            case "quit": {
                                LOG.info("Quitting");
                                break;
                            }
                            default: {
                                LOG.error(String.format("Unknown command '%s', please try again", command));
                                break;
                            }
                        }
                    }
                }
                catch (InteractiveReplyWithErrorException | InteractiveRequestNoReplyException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        finally {
            GAME_CLIENT.disconnect();
        }
    }

    /**
     * Registers Interactive event handlers for the client.
     *
     * @since   1.0.0
     */
    private static void registerEventHandlers() {

    }
}