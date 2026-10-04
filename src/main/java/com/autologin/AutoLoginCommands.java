package com.autologin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public class AutoLoginCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autologin")
                // /autologin setpass <password>
                .then(ClientCommandManager.literal("setpass")
                    .then(ClientCommandManager.argument("password", StringArgumentType.greedyString())
                        .executes(context -> {
                            String password = StringArgumentType.getString(context, "password");
                            String server = getCurrentServer();
                            if (server.equals("unknown")) {
                                context.getSource().sendFeedback(Text.literal("§6[AutoLogin]§r §cYou must be connected to a server!"));
                                return 0;
                            }
                            AutoLoginConfig.setPasswordForServer(server, password);
                            context.getSource().sendFeedback(Text.literal(
                                "§6[AutoLogin]§r §aPassword set for §e" + server + "§a!"
                            ));
                            return 1;
                        })
                    )
                )
                // /autologin default <password>
                .then(ClientCommandManager.literal("default")
                    .then(ClientCommandManager.argument("password", StringArgumentType.greedyString())
                        .executes(context -> {
                            String password = StringArgumentType.getString(context, "password");
                            AutoLoginConfig.setDefaultPassword(password);
                            context.getSource().sendFeedback(Text.literal(
                                "§6[AutoLogin]§r §aDefault password set!"
                            ));
                            return 1;
                        })
                    )
                )
                // /autologin remove
                .then(ClientCommandManager.literal("remove")
                    .executes(context -> {
                        String server = getCurrentServer();
                        if (server.equals("unknown")) {
                            context.getSource().sendFeedback(Text.literal("§6[AutoLogin]§r §cYou must be connected to a server!"));
                            return 0;
                        }
                        AutoLoginConfig.removePasswordForServer(server);
                        context.getSource().sendFeedback(Text.literal(
                            "§6[AutoLogin]§r §aPassword removed for §e" + server + "§a."
                        ));
                        return 1;
                    })
                )
                // /autologin toggle
                .then(ClientCommandManager.literal("toggle")
                    .executes(context -> {
                        boolean newState = !AutoLoginConfig.isEnabled();
                        AutoLoginConfig.setEnabled(newState);
                        context.getSource().sendFeedback(Text.literal(
                            "§6[AutoLogin]§r AutoLogin is now " + (newState ? "§aENABLED" : "§cDISABLED")
                        ));
                        return 1;
                    })
                )
                // /autologin delay <ms>
                .then(ClientCommandManager.literal("delay")
                    .then(ClientCommandManager.argument("milliseconds", IntegerArgumentType.integer(0, 10000))
                        .executes(context -> {
                            int delay = IntegerArgumentType.getInteger(context, "milliseconds");
                            AutoLoginConfig.setDelayMs(delay);
                            context.getSource().sendFeedback(Text.literal(
                                "§6[AutoLogin]§r §aLogin delay set to §e" + delay + "ms§a."
                            ));
                            return 1;
                        })
                    )
                )
                // /autologin status
                .then(ClientCommandManager.literal("status")
                    .executes(context -> {
                        String server = getCurrentServer();
                        boolean enabled = AutoLoginConfig.isEnabled();
                        boolean hasServerPass = !server.equals("unknown") && AutoLoginConfig.hasServerPassword(server);
                        boolean hasDefault = AutoLoginConfig.hasDefaultPassword();
                        int delay = AutoLoginConfig.getDelayMs();

                        StringBuilder sb = new StringBuilder();
                        sb.append("§6[AutoLogin Status]§r\n");
                        sb.append("§7Enabled: ").append(enabled ? "§aYes" : "§cNo").append("\n");
                        sb.append("§7Server: §e").append(server).append("\n");
                        sb.append("§7Server password: ").append(hasServerPass ? "§aSet" : "§cNot set").append("\n");
                        sb.append("§7Default password: ").append(hasDefault ? "§aSet" : "§cNot set").append("\n");
                        sb.append("§7Login delay: §e").append(delay).append("ms");

                        context.getSource().sendFeedback(Text.literal(sb.toString()));
                        return 1;
                    })
                )
                // /autologin (no args — show help)
                .executes(context -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("§6[AutoLogin Commands]§r\n");
                    sb.append("§e/autologin setpass <password>§7 - Set password for current server\n");
                    sb.append("§e/autologin default <password>§7 - Set default password\n");
                    sb.append("§e/autologin remove§7 - Remove password for current server\n");
                    sb.append("§e/autologin toggle§7 - Enable/disable auto-login\n");
                    sb.append("§e/autologin delay <ms>§7 - Set login delay (0-10000ms)\n");
                    sb.append("§e/autologin status§7 - Show current config");
                    context.getSource().sendFeedback(Text.literal(sb.toString()));
                    return 1;
                })
            );
        });

        AutoLoginClient.LOGGER.info("AutoLogin commands registered.");
    }

    private static String getCurrentServer() {
        MinecraftClient client = MinecraftClient.getInstance();
        ServerInfo serverInfo = client.getCurrentServerEntry();
        if (serverInfo != null) {
            return serverInfo.address;
        }
        return "unknown";
    }
}
