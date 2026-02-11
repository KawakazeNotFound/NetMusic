package com.github.tartaricacid.netmusic.api.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * VIP音乐直链API响应
 * API: https://ncm.206601.xyz/play/direct?id=XXX
 */
public class VipDirectUrl {
    @SerializedName("code")
    private int code;

    @SerializedName("success")
    private boolean success;

    @SerializedName("url")
    private String url;

    @SerializedName("song_id")
    private long songId;

    @SerializedName("level")
    private String level;

    @SerializedName("song_name")
    private String songName;

    @SerializedName("artist")
    private String artist;

    public int getCode() {
        return code;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUrl() {
        return url;
    }

    public long getSongId() {
        return songId;
    }

    public String getLevel() {
        return level;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtist() {
        return artist;
    }

    /**
     * 检查是否成功获取到有效的URL
     */
    public boolean hasValidUrl() {
        return success && code == 200 && url != null && !url.isEmpty();
    }
}
