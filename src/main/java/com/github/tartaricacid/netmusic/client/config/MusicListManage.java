package com.github.tartaricacid.netmusic.client.config;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.ExtraMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.github.tartaricacid.netmusic.api.pojo.VipDirectUrl;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class MusicListManage {
    private static final int MAX_NUM = 100;
    private static final Gson GSON = new Gson();
    private static final Path CONFIG_DIR = Paths.get("config").resolve("net_music");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("music.json");
    public static List<ItemMusicCD.SongInfo> SONGS = Lists.newArrayList();

    public static void loadConfigSongs() throws IOException {
        if (!Files.isDirectory(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }

        File file = CONFIG_FILE.toFile();
        InputStream stream = null;
        if (Files.exists(CONFIG_FILE)) {
            stream = Files.newInputStream(file.toPath());
        } else {
            ResourceLocation res = new ResourceLocation(NetMusic.MOD_ID, "music.json");
            Optional<Resource> optional = Minecraft.getInstance().getResourceManager().getResource(res);
            if (optional.isPresent()) {
                stream = optional.get().open();
            }
        }
        if (stream != null) {
            SONGS = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<List<ItemMusicCD.SongInfo>>() {
                    }.getType());
        }
    }

    public static ItemMusicCD.SongInfo get163Song(long id) throws Exception {
        NetEaseMusicSong pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.song(id), NetEaseMusicSong.class);
        ItemMusicCD.SongInfo songInfo = new ItemMusicCD.SongInfo(pojo);
        // 如果是VIP歌曲，尝试获取直链
        tryGetVipDirectUrl(id, songInfo);
        return songInfo;
    }

    public static ItemMusicCD.SongInfo getDjSong(long id) throws Exception {
        String result = NetMusic.NET_EASE_WEB_API.dj(id);
        JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
        JsonObject program = jsonObject.getAsJsonObject("program");
        if (program == null) {
            NetMusic.LOGGER.error("Failed to get DJ song info, program is null for id: {}", id);
            return new ItemMusicCD.SongInfo();
        }
        String mainSong = program.getAsJsonObject("mainSong").toString();
        if (mainSong == null) {
            NetMusic.LOGGER.error("Failed to get DJ song info, mainSong is null for id: {}", id);
            return new ItemMusicCD.SongInfo();
        }
        NetEaseMusicSong.Song netEaseMusicSong = new Gson().fromJson(mainSong, NetEaseMusicSong.Song.class);
        ItemMusicCD.SongInfo songInfo = new ItemMusicCD.SongInfo(netEaseMusicSong);
        // 如果是VIP歌曲，尝试获取直链
        tryGetVipDirectUrl(netEaseMusicSong.getId(), songInfo);
        return songInfo;
    }

    public static void add163List(long id) throws Exception {
        if (!Files.isDirectory(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }

        NetEaseMusicList pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.list(id), NetEaseMusicList.class);

        int count = pojo.getPlayList().getTracks().size();
        int size = Math.min(pojo.getPlayList().getTrackIds().size(), MAX_NUM);
        // 获取额外歌曲
        if (count < size) {
            long[] ids = new long[size - count];
            for (int i = count; i < size; i++) {
                ids[i - count] = pojo.getPlayList().getTrackIds().get(i).getId();
            }
            String extraTrackInfo = NetMusic.NET_EASE_WEB_API.songs(ids);
            ExtraMusicList extra = GSON.fromJson(extraTrackInfo, ExtraMusicList.class);
            pojo.getPlayList().getTracks().addAll(extra.getTracks());
        }

        SONGS.clear();
        for (NetEaseMusicList.Track track : pojo.getPlayList().getTracks()) {
            ItemMusicCD.SongInfo songInfo = new ItemMusicCD.SongInfo(track);
            // 如果是VIP歌曲，尝试获取直链
            tryGetVipDirectUrl(track.getId(), songInfo);
            SONGS.add(songInfo);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        FileUtils.write(CONFIG_FILE.toFile(), gson.toJson(SONGS), StandardCharsets.UTF_8);
    }

    /**
     * 尝试为VIP歌曲获取直链
     * @param songId 歌曲ID
     * @param songInfo 歌曲信息对象，如果成功获取直链会被修改
     */
    private static void tryGetVipDirectUrl(long songId, ItemMusicCD.SongInfo songInfo) {
        // 如果不是VIP歌曲，直接返回
        if (!songInfo.vip) {
            return;
        }

        // 检查配置是否启用VIP直链功能
        if (!GeneralConfig.ENABLE_VIP_DIRECT_URL.get()) {
            NetMusic.LOGGER.debug("VIP direct URL feature is disabled in config");
            return;
        }

        try {
            NetMusic.LOGGER.info("Detected VIP song (ID: {}), attempting to get direct URL...", songId);
            String response = NetMusic.NET_EASE_WEB_API.getVipDirectUrl(songId);
            VipDirectUrl vipDirectUrl = GSON.fromJson(response, VipDirectUrl.class);

            if (vipDirectUrl != null && vipDirectUrl.hasValidUrl()) {
                // 成功获取直链，更新URL并取消VIP标记
                songInfo.songUrl = vipDirectUrl.getUrl();
                songInfo.vip = false;
                NetMusic.LOGGER.info("Successfully obtained direct URL for VIP song: {} (Level: {})", 
                    songInfo.songName, vipDirectUrl.getLevel());
            } else {
                NetMusic.LOGGER.warn("Failed to get direct URL for VIP song: {} (ID: {}), keeping VIP flag", 
                    songInfo.songName, songId);
            }
        } catch (Exception e) {
            NetMusic.LOGGER.error("Error while trying to get VIP direct URL for song ID: {}", songId, e);
            // 保持VIP标记，歌曲将无法播放
        }
    }
}
