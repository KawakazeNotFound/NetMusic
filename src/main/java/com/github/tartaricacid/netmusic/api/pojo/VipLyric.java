package com.github.tartaricacid.netmusic.api.pojo;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * VIP歌词API响应
 * API: https://ncm.206601.xyz/play/vrc?id=XXX
 */
public class VipLyric {
    private static final Pattern JSON_LINE_PATTERN = Pattern.compile("^\\{\"t\":\\d+,\"c\":\\[.*?]}\\s*$");

    @SerializedName("songName")
    private String songName;

    @SerializedName("artist")
    private String artist;

    @SerializedName("lyric")
    private String lyric;

    @SerializedName("transLyric")
    private String transLyric;

    public String getSongName() {
        return songName;
    }

    public String getArtist() {
        return artist;
    }

    /**
     * 获取原始歌词（已清理JSON行）
     */
    public String getLyric() {
        return cleanLyric(lyric);
    }

    /**
     * 获取翻译歌词
     */
    public String getTransLyric() {
        return transLyric;
    }

    /**
     * 清理歌词中的JSON格式行，只保留LRC格式内容
     */
    private static String cleanLyric(String lyricContent) {
        if (StringUtils.isBlank(lyricContent)) {
            return "";
        }

        StringBuilder cleaned = new StringBuilder();
        String[] lines = lyricContent.split("\n");

        for (String line : lines) {
            // 跳过JSON格式的行（如 {"t":0,"c":[...]}）
            if (!JSON_LINE_PATTERN.matcher(line.trim()).matches()) {
                cleaned.append(line).append("\n");
            }
        }

        return cleaned.toString();
    }

    /**
     * 检查是否有有效的歌词
     */
    public boolean hasLyric() {
        return StringUtils.isNotBlank(getLyric());
    }

    /**
     * 检查是否有翻译歌词
     */
    public boolean hasTransLyric() {
        return StringUtils.isNotBlank(transLyric);
    }
}
