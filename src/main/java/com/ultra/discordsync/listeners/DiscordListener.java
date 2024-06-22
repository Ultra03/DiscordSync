package com.ultra.discordsync.listeners;

import com.ultra.discordsync.Config;
import cpw.mods.fml.common.FMLCommonHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import javax.annotation.Nonnull;
import java.util.List;

public class DiscordListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.getChannel().getId().equals(Config.mcChannelId)) return;

        String message = event.getMessage().getContentStripped();
        ChatStyle style = new ChatStyle().setColor(EnumChatFormatting.AQUA);
        if (event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId()) || event.isWebhookMessage()) return;

        List<EntityPlayerMP> onlinePlayers = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;
        for (EntityPlayerMP player : onlinePlayers) {
            player.addChatMessage(new ChatComponentText(String.format("[D] <%s> %s", event.getAuthor().getEffectiveName(), message)).setChatStyle(style));
        }
    }

}
