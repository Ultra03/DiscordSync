package com.ultra.discordsync.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.ultra.discordsync.Config;
import com.ultra.discordsync.DiscordSync;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class MinecraftListener {

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        if (event.player == null) return;
        if (event.player instanceof FakePlayer) return;

        JDA jda = DiscordSync.getInstance().getJda();
        if (jda == null) return;

        TextChannel channel = jda.getTextChannelById(Config.mcChannelId);
        if (channel == null) return;

        EntityPlayerMP player = event.player;

        if (Config.useWebhook) {
            try (WebhookClient client = DiscordSync.getInstance().getWebhookBuilder().build()) {
                WebhookMessage hookMsg = new WebhookMessageBuilder()
                        .setAvatarUrl(DiscordSync.getAvatarUrl(player.getUniqueID()))
                        .setUsername(player.getDisplayName())
                        .setContent(event.message)
                        .build();
                client.send(hookMsg);
            }
        } else {
            channel.sendMessage(String.format("**%s:** %s", player.getDisplayName(), event.message)).complete();
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;

        JDA jda = DiscordSync.getInstance().getJda();
        if (jda == null) return;

        TextChannel channel = DiscordSync.getInstance().getJda().getTextChannelById(Config.mcChannelId);
        if (channel == null) return;

        IChatComponent deathMessage = event.source.func_151519_b(event.entityLiving);
        String deathStr = deathMessage.getUnformattedText();

        channel.sendMessage(String.format("**%s**", deathStr)).complete();
    }

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        System.out.println("logged in");
        EntityPlayer player = (EntityPlayer) event.player;

        JDA jda = DiscordSync.getInstance().getJda();
        if (jda == null) return;

        TextChannel channel = DiscordSync.getInstance().getJda().getTextChannelById(Config.mcChannelId);
        if (channel == null) return;

        channel.sendMessage(String.format("**%s joined the game**", player.getDisplayName())).complete();
    }

    @SubscribeEvent
    public void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        EntityPlayer player = event.player;

        JDA jda = DiscordSync.getInstance().getJda();
        if (jda == null) return;

        TextChannel channel = DiscordSync.getInstance().getJda().getTextChannelById(Config.mcChannelId);
        if (channel == null) return;

        channel.sendMessage(String.format("**%s left the game**", player.getDisplayName())).complete();
    }

}
