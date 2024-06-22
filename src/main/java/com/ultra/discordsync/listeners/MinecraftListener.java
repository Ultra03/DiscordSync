package com.ultra.discordsync.listeners;

import com.ultra.discordsync.Config;
import com.ultra.discordsync.DiscordSync;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ServerChatEvent;

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
        System.out.printf("%s: %s%n", player.getDisplayName(), event.message);
        channel.sendMessage(String.format("**%s:** %s", player.getDisplayName(), event.message)).complete();
    }

}
