package com.bunubbv.gatekeeper.fabric;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final File CONFIG_FILE = new File("config/gatekeeper.yml");

    public static String answer;
    public static String question;
    public static String welcomeMessage;
    public static String correctMessage;
    public static String incorrectMessage;
    public static String kickMessage;
    public static int kickDelay;

    private static final String DEFAULT_ANSWER = "16";
    private static final String DEFAULT_QUESTION = "Evaluate the definite integral of 4 with respect to x, from 0 to 4.";
    private static final String DEFAULT_WELCOME = "Welcome! Please answer the following question to join the server:";
    private static final String DEFAULT_CORRECT = "Correct! You're now verified and ready to play.";
    private static final String DEFAULT_INCORRECT = "Incorrect answer. Please try again.";
    private static final String DEFAULT_KICK = "You didn't answer in time. Please try again later.";
    private static final int DEFAULT_KICK_DELAY = 3;

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            saveDefaults();
        }

        Map<String, Object> configData = loadYaml();

        answer = configData.getOrDefault("answer", DEFAULT_ANSWER).toString();
        question = configData.getOrDefault("question", DEFAULT_QUESTION).toString();
        welcomeMessage = configData.getOrDefault("welcomeMessage", DEFAULT_WELCOME).toString();
        correctMessage = configData.getOrDefault("correctMessage", DEFAULT_CORRECT).toString();
        incorrectMessage = configData.getOrDefault("incorrectMessage", DEFAULT_INCORRECT).toString();
        kickMessage = configData.getOrDefault("kickMessage", DEFAULT_KICK).toString();

        try {
            kickDelay = Integer.parseInt(configData.getOrDefault("kickDelay", DEFAULT_KICK_DELAY).toString());
        } catch (NumberFormatException e) {
            kickDelay = DEFAULT_KICK_DELAY;
        }
    }

    private static Map<String, Object> loadYaml() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(input);
            if (loaded instanceof Map) {
                return (Map<String, Object>) loaded;
            }
        } catch (IOException e) {
            System.err.println("Error loading gatekeeper.yml: " + e.getMessage());
        }
        return new HashMap<>();
    }

    private static void saveDefaults() {
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("answer", DEFAULT_ANSWER);
        defaultConfig.put("question", DEFAULT_QUESTION);
        defaultConfig.put("welcomeMessage", DEFAULT_WELCOME);
        defaultConfig.put("correctMessage", DEFAULT_CORRECT);
        defaultConfig.put("incorrectMessage", DEFAULT_INCORRECT);
        defaultConfig.put("kickMessage", DEFAULT_KICK);
        defaultConfig.put("kickDelay", DEFAULT_KICK_DELAY);

        saveYaml(defaultConfig);
    }

    private static void saveYaml(Map<String, Object> data) {
        File parentDir = CONFIG_FILE.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            return;
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            yaml.dump(data, writer);
        } catch (IOException e) {
            System.err.println("Error saving gatekeeper.yml: " + e.getMessage());
        }
    }
}
