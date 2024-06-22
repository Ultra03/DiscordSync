package com.ultra.discordsync;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class Config {
    
    private static class Defaults {
        public static final String greeting = "Hello World";
        public static final String botToken = "";
        public static final String mcChannelId = "";
    }

    private static class Categories {
        public static final String general = "general";
    }
    
    public static String greeting = Defaults.greeting;
    public static String botToken = Defaults.botToken;
    public static String mcChannelId = Defaults.mcChannelId;

    public static void syncronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);
        configuration.load();
        
        Property greetingProperty = configuration.get(Categories.general, "greeting", Defaults.greeting, "How shall I greet?");
        greeting = greetingProperty.getString();

        Property tokenProperty = configuration.get(Categories.general, "botToken", Defaults.botToken);
        botToken = tokenProperty.getString();

        Property mcChannelIdProperty = configuration.get(Categories.general, "mcChannelId", Defaults.mcChannelId);
        mcChannelId = mcChannelIdProperty.getString();

        if(configuration.hasChanged()) {
            configuration.save();
        }
    }
}