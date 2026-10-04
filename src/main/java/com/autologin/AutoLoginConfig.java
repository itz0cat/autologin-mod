package com.autologin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AutoLoginConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autologin.json");

    private static ConfigData data = new ConfigData();

    public static class ConfigData {
        public boolean enabled = true;
        public String defaultPassword = "";
        public int delayMs = 500;
        public List<String> triggerPhrases = new ArrayList<>(Arrays.asList(
                "/login",
                "please login",
                "please log in",
                "use /login",
                "введите /login",
                "пожалуйста, авторизуйтесь"
        ));
        public Map<String, String> serverPasswords = new LinkedHashMap<>();
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                data = GSON.fromJson(json, ConfigData.class);
                if (data == null) data = new ConfigData();
                AutoLoginClient.LOGGER.info("AutoLogin config loaded.");
            } catch (IOException e) {
                AutoLoginClient.LOGGER.error("Failed to load AutoLogin config", e);
                data = new ConfigData();
            }
        } else {
            save(); // create default config
            AutoLoginClient.LOGGER.info("AutoLogin default config created.");
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            AutoLoginClient.LOGGER.error("Failed to save AutoLogin config", e);
        }
    }

    public static boolean isEnabled() {
        return data.enabled;
    }

    public static void setEnabled(boolean enabled) {
        data.enabled = enabled;
        save();
    }

    public static String getPasswordForServer(String serverAddress) {
        // Strip port if present for lookup
        String host = serverAddress.contains(":") ? serverAddress.split(":")[0] : serverAddress;
        String password = data.serverPasswords.get(host);
        if (password != null && !password.isEmpty()) {
            return password;
        }
        // Try with original address (with port)
        password = data.serverPasswords.get(serverAddress);
        if (password != null && !password.isEmpty()) {
            return password;
        }
        return data.defaultPassword;
    }

    public static void setPasswordForServer(String serverAddress, String password) {
        String host = serverAddress.contains(":") ? serverAddress.split(":")[0] : serverAddress;
        data.serverPasswords.put(host, password);
        save();
    }

    public static void removePasswordForServer(String serverAddress) {
        String host = serverAddress.contains(":") ? serverAddress.split(":")[0] : serverAddress;
        data.serverPasswords.remove(host);
        data.serverPasswords.remove(serverAddress);
        save();
    }

    public static void setDefaultPassword(String password) {
        data.defaultPassword = password;
        save();
    }

    public static int getDelayMs() {
        return data.delayMs;
    }

    public static void setDelayMs(int delayMs) {
        data.delayMs = Math.max(0, delayMs);
        save();
    }

    public static List<String> getTriggerPhrases() {
        return data.triggerPhrases;
    }

    public static boolean hasServerPassword(String serverAddress) {
        String host = serverAddress.contains(":") ? serverAddress.split(":")[0] : serverAddress;
        return data.serverPasswords.containsKey(host) || data.serverPasswords.containsKey(serverAddress);
    }

    public static boolean hasDefaultPassword() {
        return data.defaultPassword != null && !data.defaultPassword.isEmpty();
    }

    public static String getDefaultPassword() {
        return data.defaultPassword;
    }
}
