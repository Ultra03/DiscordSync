package com.ultra.discordsync;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.neovisionaries.ws.client.DualStackMode;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.ultra.discordsync.listeners.DiscordListener;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]", acceptableRemoteVersions = "*")
public class DiscordSync {

    private static Logger LOG = LogManager.getLogger(Tags.MODID);
    private static final Collection<GatewayIntent> INTENTS = Arrays.asList(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_WEBHOOKS);

    @SidedProxy(clientSide= Tags.GROUPNAME + ".ClientProxy", serverSide=Tags.GROUPNAME + ".CommonProxy")
    public static CommonProxy proxy;

    private static DiscordSync instance = null;

    private JDA jda = null;

    public DiscordSync() {
        // this is terrible but needed
        instance = this;
    }

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items,
    // etc, and register them with the GameRegistry."
    public void preInit(FMLPreInitializationEvent event) {
        Config.syncronizeConfiguration(event.getSuggestedConfigurationFile());
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes."
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this."
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        proxy.serverAboutToStart(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler
    public void serverStarting(FMLServerStartingEvent event) {
        Thread initThread = new Thread(this::discordInit, "DiscordSync Init");
        initThread.start();
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        proxy.serverStarted(event);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        if (jda != null) {
            TextChannel channel = jda.getTextChannelById(Config.mcChannelId);
            channel.sendMessage("**Server stopped**").complete();
            jda.shutdownNow();
            jda = null;
        }
        proxy.serverStopping(event);
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        proxy.serverStopped(event);
    }

    public static void debug(String message) {
        LOG.debug(message);
    }

    public static void info(String message) {
        LOG.info(message);
    }

    public static void warn(String message) {
        LOG.warn(message);
    }

    public static void error(String message) {
        LOG.error(message);
    }

    private void discordInit() {
        try {
            if (Config.botToken.isEmpty()) {
                LOG.error("Invalid bot token, disabling Discord integration");
                return;
            }

            if (Config.mcChannelId.isEmpty()) {
                LOG.error("Invalid channel ID, disabling Discord integration");
                return;
            }

            // Thanks to DiscordSRV for thread and OkHttp settings
            Dispatcher dispatcher = new Dispatcher(
                    new ThreadPoolExecutor(2, 20, 5, TimeUnit.SECONDS,
                            new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false))
            );
            dispatcher.setMaxRequests(20);
            dispatcher.setMaxRequestsPerHost(20);
            ConnectionPool connPool = new ConnectionPool(5, 10, TimeUnit.SECONDS);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .dispatcher(dispatcher)
                    .connectionPool(connPool)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build();

            final ForkJoinPool callbackPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), pool -> {
                final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                worker.setName("DiscordSync - Callback Worker");
                return worker;
            }, null, true);

            final ThreadFactory gatewayFactory = new ThreadFactoryBuilder().setNameFormat("DiscordSync - Gateway").build();
            final ScheduledExecutorService gatewayPool = Executors.newSingleThreadScheduledExecutor(gatewayFactory);

            final ThreadFactory ratelimitFactory = new ThreadFactoryBuilder().setNameFormat("DiscordSync - Rate Limits").build();
            final ScheduledExecutorService ratelimitPool = Executors.newSingleThreadScheduledExecutor(ratelimitFactory);

            jda = JDABuilder.create(INTENTS)
                    .setCallbackPool(callbackPool)
                    .setGatewayPool(gatewayPool)
                    .setRateLimitPool(ratelimitPool)
                    .setWebsocketFactory(new WebSocketFactory().setDualStackMode(DualStackMode.IPV4_ONLY))
                    .setHttpClient(httpClient)
                    .setAutoReconnect(true)
                    .setEnableShutdownHook(false)
                    .addEventListeners(new DiscordListener())
                    .setToken(Config.botToken)
                    .build();

            jda.awaitReady();

            TextChannel channel = jda.getChannelById(TextChannel.class, Config.mcChannelId);
            if (channel == null) {
                LOG.error("Invalid Discord channel, disabling Discord integration");
                jda.shutdownNow();
                jda = null;
            } else {
                channel.sendMessage("**Server started**").complete();
            }
        } catch (InterruptedException e) {
            LOG.error("Error occurred while initializing JDA", e);
        }
    }

    public JDA getJda() {
        return jda;
    }

    public static DiscordSync getInstance() {
        return instance;
    }
}