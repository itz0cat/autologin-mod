package com.autologin;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatListener {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "AutoLogin-Scheduler");
        t.setDaemon(true);
        return t;
    });

    private static long lastLoginAttempt = 0;
    private static final long COOLDOWN_MS = 5000; // 5 second cooldown between login attempts

    public static void register() {
        // Listen for GAME messages (system messages from plugins)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!AutoLoginConfig.isEnabled()) return;
            if (overlay) return; // skip action bar messages

            String content = message.getString().toLowerCase();
            boolean triggered = false;

            for (String trigger : AutoLoginConfig.getTriggerPhrases()) {
                if (content.contains(trigger.toLowerCase())) {
                    triggered = true;
                    break;
                }
            }

            if (triggered) {
                long now = System.currentTimeMillis();
                if (now - lastLoginAttempt < COOLDOWN_MS) {
                    return; // cooldown active
                }
                lastLoginAttempt = now;

                String serverAddress = getServerAddress();
                String password = AutoLoginConfig.getPasswordForServer(serverAddress);

                if (password == null || password.isEmpty()) {
                    AutoLoginClient.LOGGER.warn("Login prompt detected but no password configured for server: {}", serverAddress);
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.player != null) {
                        client.player.sendMessage(
                            Text.literal("§6[AutoLogin]§r §cNo password set for this server! Use §e/autologin setpass <password>§c to set one."),
                            false
                        );
                    }
                    return;
                }

                int delay = AutoLoginConfig.getDelayMs();
                AutoLoginClient.LOGGER.info("Login prompt detected! Sending /login in {}ms for server: {}", delay, serverAddress);

                SCHEDULER.schedule(() -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.getNetworkHandler() != null) {
                        // sendCommand does NOT need the leading slash
                        client.execute(() -> {
                            client.getNetworkHandler().sendCommand("login " + password);
                            if (client.player != null) {
                                client.player.sendMessage(
                                    Text.literal("§6[AutoLogin]§r §aLogin command sent automatically!"),
                                    false
                                );
                            }
                        });
                    }
                }, delay, TimeUnit.MILLISECONDS);
            }
        });

        // Also listen for CHAT messages (some servers send login prompts as chat)
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!AutoLoginConfig.isEnabled()) return;

            String content = message.getString().toLowerCase();
            boolean triggered = false;

            for (String trigger : AutoLoginConfig.getTriggerPhrases()) {
                if (content.contains(trigger.toLowerCase())) {
                    triggered = true;
                    break;
                }
            }

            if (triggered) {
                long now = System.currentTimeMillis();
                if (now - lastLoginAttempt < COOLDOWN_MS) {
                    return;
                }
                lastLoginAttempt = now;

                String serverAddress = getServerAddress();
                String password = AutoLoginConfig.getPasswordForServer(serverAddress);

                if (password == null || password.isEmpty()) {
                    return; // silently skip if no password
                }

                int delay = AutoLoginConfig.getDelayMs();
                SCHEDULER.schedule(() -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.getNetworkHandler() != null) {
                        client.execute(() -> {
                            client.getNetworkHandler().sendCommand("login " + password);
                            if (client.player != null) {
                                client.player.sendMessage(
                                    Text.literal("§6[AutoLogin]§r §aLogin command sent automatically!"),
                                    false
                                );
                            }
                        });
                    }
                }, delay, TimeUnit.MILLISECONDS);
            }
        });

        AutoLoginClient.LOGGER.info("AutoLogin chat listener registered.");
    }

    private static String getServerAddress() {
        MinecraftClient client = MinecraftClient.getInstance();
        ServerInfo serverInfo = client.getCurrentServerEntry();
        if (serverInfo != null) {
            return serverInfo.address;
        }
        // Fallback — try to get from network handler
        if (client.getNetworkHandler() != null && client.getNetworkHandler().getConnection() != null) {
            return client.getNetworkHandler().getConnection().getAddress().toString();
        }
        return "unknown";
    }
}
