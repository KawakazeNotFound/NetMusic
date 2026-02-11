package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class GetMusicListMessage {
    public static final long RELOAD_MESSAGE = -1;
    private final long musicListId;

    public GetMusicListMessage(long musicListId) {
        this.musicListId = musicListId;
    }

    public static GetMusicListMessage decode(FriendlyByteBuf buf) {
        return new GetMusicListMessage(buf.readLong());
    }

    public static void encode(GetMusicListMessage message, FriendlyByteBuf buf) {
        buf.writeLong(message.musicListId);
    }

    public static void handle(GetMusicListMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (message.musicListId == RELOAD_MESSAGE) {
                    // 重载配置是快速操作，不需要异步
                    try {
                        MusicListManage.loadConfigSongs();
                        if (player != null) {
                            player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.reload.success"));
                        }
                    } catch (Exception e) {
                        if (player != null) {
                            player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.reload.fail").withStyle(ChatFormatting.RED));
                        }
                        e.printStackTrace();
                    }
                } else {
                    // 异步加载歌单，避免阻塞主线程
                    if (player != null) {
                        player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.loading").withStyle(ChatFormatting.YELLOW));
                    }
                    CompletableFuture.runAsync(() -> {
                        try {
                            MusicListManage.add163List(message.musicListId);
                        } catch (Exception e) {
                            Minecraft.getInstance().execute(() -> {
                                if (player != null) {
                                    player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.fail").withStyle(ChatFormatting.RED));
                                }
                            });
                            e.printStackTrace();
                            return;
                        }
                        Minecraft.getInstance().execute(() -> {
                            if (player != null) {
                                player.sendSystemMessage(Component.translatable("command.netmusic.music_cd.add163.success"));
                            }
                        });
                    }, Util.backgroundExecutor());
                }
            });
        }
        context.setPacketHandled(true);
    }
}
