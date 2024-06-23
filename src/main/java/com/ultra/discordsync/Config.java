package com.ultra.discordsync;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class Config {
    
    private static class Defaults {
        public static final String botToken = "";
        public static final String mcChannelId = "";
        public static final boolean useWebhook = false;
    }

    private static class Categories {
        public static final String general = "general";
    }

    public static String botToken = Defaults.botToken;
    public static String mcChannelId = Defaults.mcChannelId;
    public static boolean useWebhook = Defaults.useWebhook;

    public static void syncronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);
        configuration.load();

        Property tokenProperty = configuration.get(Categories.general, "botToken", Defaults.botToken);
        botToken = tokenProperty.getString();

        Property mcChannelIdProperty = configuration.get(Categories.general, "mcChannelId", Defaults.mcChannelId);
        mcChannelId = mcChannelIdProperty.getString();

        Property useWebhookProperty = configuration.get(Categories.general, "useWebhook", Defaults.useWebhook);
        useWebhook = useWebhookProperty.getBoolean();

        if(configuration.hasChanged()) {
            configuration.save();
        }
    }

}