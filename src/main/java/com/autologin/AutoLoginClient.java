package com.autologin;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoLoginClient implements ClientModInitializer {
    public static final String MOD_ID = "autologin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("AutoLogin mod initialized!");
        AutoLoginConfig.load();
        ChatListener.register();
        AutoLoginCommands.register();
    }
}
